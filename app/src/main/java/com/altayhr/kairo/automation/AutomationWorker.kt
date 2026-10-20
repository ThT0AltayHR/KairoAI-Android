package com.altayhr.kairo.automation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

class AutomationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val id = inputData.getString(KEY_ID) ?: return Result.failure()
        val event = inputData.getString(KEY_EVENT) ?: "schedule"
        val payload = inputData.getString(KEY_PAYLOAD) ?: "{}"
        val store = AutomationStore(applicationContext)
        val workflow = store.workflows().firstOrNull { it.id == id } ?: return Result.failure()
        if (!workflow.enabled) return Result.success()
        val run = AutomationExecutor.run(applicationContext, workflow, event, payload)
        store.updateRun(id, run.message)
        if (!run.success || workflow.notifyOnResult) notifyResult(applicationContext, workflow, run.success, run.message)
        AutomationEvents.recordOnly(applicationContext, if (run.success) "automation.success" else "automation.failure", "${workflow.name}: ${run.message}")
        applicationContext.sendBroadcast(android.content.Intent(ACTION_UPDATED).setPackage(applicationContext.packageName))
        return if (run.success) Result.success() else if (run.httpCode in 500..599 || run.message.startsWith("İstek başarısız:")) Result.retry() else Result.failure()
    }

    companion object {
        const val ACTION_UPDATED = "com.altayhr.kairo.automation.UPDATED"
        const val KEY_ID = "workflow_id"
        const val KEY_EVENT = "event_type"
        const val KEY_PAYLOAD = "payload_json"

        private fun notifyResult(context: Context, workflow: AutomationWorkflow, success: Boolean, detail: String) {
            runCatching {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    val channel = android.app.NotificationChannel("kairo_automation_results", "Otomasyon sonuçları", android.app.NotificationManager.IMPORTANCE_DEFAULT)
                    channel.description = "Kairo AI iş akışlarının başarı ve hata bildirimleri"
                    manager.createNotificationChannel(channel)
                }
                val open = android.app.PendingIntent.getActivity(
                    context, workflow.id.hashCode(),
                    android.content.Intent(context, com.altayhr.kairo.MainActivity::class.java)
                        .putExtra("kairo_route", "automation")
                        .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP),
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                val notification = androidx.core.app.NotificationCompat.Builder(context, "kairo_automation_results")
                    .setSmallIcon(com.altayhr.kairo.R.drawable.ic_status)
                    .setContentTitle(if (success) "Otomasyon tamamlandı" else "Otomasyon başarısız")
                    .setContentText("${workflow.name}: ${detail.take(130)}")
                    .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText("${workflow.name}\n${detail.take(500)}"))
                    .setContentIntent(open).setAutoCancel(true)
                    .setCategory(androidx.core.app.NotificationCompat.CATEGORY_STATUS)
                    .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
                    .build()
                manager.notify(41_000 + (workflow.id.hashCode() and 0x0fff), notification)
            }
        }

        fun oneTime(context: Context, workflow: AutomationWorkflow, event: String, payloadJson: String = "{}") {
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = OneTimeWorkRequestBuilder<AutomationWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_ID to workflow.id, KEY_EVENT to event, KEY_PAYLOAD to payloadJson))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork("kairo-auto-${workflow.id}-${System.currentTimeMillis()}", ExistingWorkPolicy.KEEP, request)
        }

        fun schedule(context: Context, workflow: AutomationWorkflow) {
            val manager = WorkManager.getInstance(context)
            if (!workflow.enabled || workflow.trigger != "schedule") {
                manager.cancelUniqueWork("kairo-schedule-${workflow.id}")
                return
            }
            val interval = workflow.intervalMinutes.coerceIn(15, 10080).toLong()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val request = PeriodicWorkRequestBuilder<AutomationWorker>(interval, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_ID to workflow.id, KEY_EVENT to "schedule", KEY_PAYLOAD to "{}"))
                .build()
            manager.enqueueUniquePeriodicWork("kairo-schedule-${workflow.id}", ExistingPeriodicWorkPolicy.UPDATE, request)
        }
    }
}

object AutomationEvents {
    fun emit(context: Context, type: String, detail: String, payloadJson: String = "{}") {
        recordOnly(context, type, detail)
        AutomationStore(context).workflows().filter { it.enabled && it.trigger == "event" && it.triggerValue == type }.forEach {
            AutomationWorker.oneTime(context, it, type, payloadJson)
        }
    }
    internal fun recordOnly(context: Context, type: String, detail: String) {
        AutomationStore(context).appendEvent(AutomationEvent(java.util.UUID.randomUUID().toString(), type, detail.take(400)))
    }
}
