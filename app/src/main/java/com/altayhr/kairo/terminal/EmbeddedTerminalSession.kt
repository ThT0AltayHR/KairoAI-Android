package com.altayhr.kairo.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient
import java.io.File
import java.util.LinkedHashMap

/** Shared PTY sessions stay alive while the user changes screens. A chat gets its own cwd/session. */
object EmbeddedTerminalSessions {
    private const val MAX_LIVE_SESSIONS = 8
    private val sessions = object : LinkedHashMap<String, EmbeddedTerminalSession>(16, .75f, true) {}

    @Synchronized
    fun get(context: Context, sessionId: String): EmbeddedTerminalSession {
        val key = sessionId.ifBlank { "personal" }
        sessions[key]?.let { return it }
        while (sessions.size >= MAX_LIVE_SESSIONS) {
            val oldestKey = sessions.keys.firstOrNull() ?: break
            sessions.remove(oldestKey)?.close()
        }
        return EmbeddedTerminalSession(context.applicationContext, key).also { sessions[key] = it }
    }

    @Synchronized
    fun restart(context: Context, sessionId: String): EmbeddedTerminalSession {
        val key = sessionId.ifBlank { "personal" }
        sessions.remove(key)?.close()
        return get(context.applicationContext, key)
    }
}

class EmbeddedTerminalSession internal constructor(
    private val context: Context,
    val sessionId: String
) {
    private val workspace: File = EmbeddedTerminalEngine.workspace(context, sessionId)
    private val homeDirectory: File = EmbeddedTerminalEngine.homeDirectory(context, sessionId)
    private val tempDirectory: File = EmbeddedTerminalEngine.tempDirectory(context, sessionId)
    @Volatile private var attachedView: TerminalView? = null
    @Volatile var exitMessage: String? = null
        private set
    private var fontSize: Int = 13

    private val sessionClient = object : TerminalSessionClient {
        override fun onTextChanged(changedSession: TerminalSession) { attachedView?.postInvalidate() }
        override fun onTitleChanged(changedSession: TerminalSession) { attachedView?.postInvalidate() }
        override fun onSessionFinished(finishedSession: TerminalSession) {
            exitMessage = "Kabuk kapandı · çıkış kodu ${finishedSession.getExitStatus()}"
            attachedView?.postInvalidate()
        }
        override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Kairo terminal", text))
        }
        override fun onPasteTextFromClipboard(session: TerminalSession?) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val text = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
            val bytes = text.toByteArray(Charsets.UTF_8)
            session?.write(bytes, 0, bytes.size)
        }
        override fun onBell(session: TerminalSession) { attachedView?.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP) }
        override fun onColorsChanged(session: TerminalSession) { attachedView?.postInvalidate() }
        override fun onTerminalCursorStateChange(state: Boolean) { attachedView?.postInvalidate() }
        override fun setTerminalShellPid(session: TerminalSession, pid: Int) { exitMessage = null }
        override fun getTerminalCursorStyle(): Int = TerminalEmulator.TERMINAL_CURSOR_STYLE_BLOCK
        override fun logError(tag: String, message: String) {}
        override fun logWarn(tag: String, message: String) {}
        override fun logInfo(tag: String, message: String) {}
        override fun logDebug(tag: String, message: String) {}
        override fun logVerbose(tag: String, message: String) {}
        override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
        override fun logStackTrace(tag: String, e: Exception) {}
    }

    val terminalSession: TerminalSession = TerminalSession(
        "/system/bin/sh",
        workspace.absolutePath,
        arrayOf("/system/bin/sh", "-i"),
        arrayOf(
            "HOME=${homeDirectory.absolutePath}",
            "TMPDIR=${tempDirectory.absolutePath}",
            "PATH=${EmbeddedTerminalEngine.toolDirectory(context).absolutePath}:/system/bin:/system/xbin",
            "TERM=xterm-256color",
            "COLORTERM=truecolor",
            "LANG=C.UTF-8",
            "PWD=${workspace.absolutePath}",
            "ANDROID_ROOT=/system",
            "ANDROID_DATA=/data"
        ),
        2000,
        sessionClient
    ).apply { mSessionName = "Kairo · ${sessionId.take(24)}" }

    fun attach(view: TerminalView) {
        attachedView = view
        view.setBackgroundColor(Color.rgb(10, 13, 19))
        view.setTextSize(13)
        view.setKeepScreenOn(true)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.setTerminalViewClient(object : TerminalViewClient {
            override fun onScale(scale: Float): Float {
                fontSize = (fontSize * scale).toInt().coerceIn(9, 22)
                view.setTextSize(fontSize)
                return 1f
            }
            override fun onSingleTapUp(e: MotionEvent) {
                view.requestFocus()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
            }
            override fun shouldBackButtonBeMappedToEscape() = false
            override fun shouldEnforceCharBasedInput() = false
            override fun shouldUseCtrlSpaceWorkaround() = false
            override fun isTerminalViewSelected() = true
            override fun copyModeChanged(copyMode: Boolean) {}
            override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession) = false
            override fun onKeyUp(keyCode: Int, e: KeyEvent) = false
            override fun onLongPress(event: MotionEvent) = false
            override fun readControlKey() = false
            override fun readAltKey() = false
            override fun readShiftKey() = false
            override fun readFnKey() = false
            override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession) = false
            override fun onEmulatorSet() { view.postInvalidate() }
            override fun logError(tag: String, message: String) {}
            override fun logWarn(tag: String, message: String) {}
            override fun logInfo(tag: String, message: String) {}
            override fun logDebug(tag: String, message: String) {}
            override fun logVerbose(tag: String, message: String) {}
            override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) {}
            override fun logStackTrace(tag: String, e: Exception) {}
        })
        view.attachSession(terminalSession)
    }

    fun detach(view: TerminalView) {
        if (attachedView === view) attachedView = null
        view.setKeepScreenOn(false)
    }

    fun close() {
        attachedView?.setKeepScreenOn(false)
        attachedView = null
        terminalSession.finishIfRunning()
    }
}
