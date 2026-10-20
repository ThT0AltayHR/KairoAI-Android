package com.altayhr.kairo.service

import android.content.Context
import com.altayhr.kairo.data.ChatMessage
import com.altayhr.kairo.data.ChatSession
import com.altayhr.kairo.data.SecureApiKeyStore
import com.altayhr.kairo.data.WorkspaceStore
import com.altayhr.kairo.network.GeminiClient

/** Optional multi-agent planning/review. Agent responses are concise task artifacts, never private chain-of-thought. */
object AgentOrchestrator {
    suspend fun prepare(
        context: Context,
        chat: ChatSession,
        client: GeminiClient,
        secrets: SecureApiKeyStore,
        model: String,
        webSearch: Boolean,
        thinking: String,
        onStage: suspend (String) -> Unit
    ): String {
        val userRequest = chat.messages.lastOrNull { it.role == "user" }?.content.orEmpty()
        if (userRequest.isBlank()) return ""
        val longTask = userRequest.length > 180 || listOf("proje", "uygula", "oluştur", "refactor", "entegr", "derle", "hata", "dosyaları").any { userRequest.contains(it, true) }
        val codeTask = listOf("kod", "swift", "kotlin", "java", "python", "typescript", "html", "css", "gradle", "api", "dosya oluştur", "fonksiyon").any { userRequest.contains(it, true) }
        val integrationTask = listOf("entegr", "bağla", "oauth", "webhook", "github", "spotify", "api", "worker").any { userRequest.contains(it, true) }
        val pieces = mutableListOf<String>()
        suspend fun runRole(slot: Int, title: String, prompt: String) {
            val key = secrets.getSecret("gemini_agent_key_$slot") ?: return
            try {
                onStage("$title çalışıyor")
                val synthetic = chat.copy(messages = chat.messages + ChatMessage(role = "user", content = prompt))
                val result = client.reply(synthetic, key, model, false, thinking).text.trim()
                if (result.isNotBlank()) pieces += "[$title · uygulanabilir özet]\n${result.take(2200)}"
            } catch (_: Exception) {
                // Optional specialist failure must not discard the user's main response.
                pieces += "[$title] Uzman çağrısı başarısız oldu; ana ajan devam ediyor."
            }
        }
        if (longTask) runRole(7, "Görev dağıtıcı", "Kullanıcının isteği için kısa bir görev planı üret. Yalnızca somut adımlar, bağımlılıklar, kabul ölçütleri ve önemli riskleri yaz. Gizli düşünce zinciri yazma; 8 maddeden fazla olmasın. İstek: $userRequest")
        if (longTask) runRole(2, "Planlama ajanı", "İsteği bağımsız, doğrulanabilir alt görevlere böl. Sıralama, tamamlanma ölçütü ve hata durumunda geri dönüş yolunu belirt. Yalnızca kısa görev listesi ver. İstek: $userRequest")
        if (codeTask) runRole(3, "Kodlama ajanı", "Kullanıcının kod/proje isteği için teknik yaklaşımı ve kritik dosya değişikliklerini özetle. Kod yazılması istenmişse gerekli API/sınıf tasarımını belirt; uygulanmamış bir değişikliği uygulanmış gibi sunma. İstek: $userRequest")
        if (integrationTask) runRole(6, "Entegrasyon ajanı", "Bu istekteki servis/API entegrasyonlarını, izinleri, hata durumlarını ve güvenli secret kullanımını kısa bir kontrol listesi olarak ver. İstek: $userRequest")
        if (longTask) runRole(8, "Kapsül/widget ajanı", "Bu görevin kapsülde ve widget'ta gösterilmesi için en fazla iki satırlık bir durum özeti ve bir sonraki doğrulanabilir kilometre taşını öner. Gerçek ölçüm yoksa yüzde veya kesin kalan süre uydurma. İstek: $userRequest")
        return pieces.joinToString("\n\n")
    }

    suspend fun review(
        chat: ChatSession,
        answer: String,
        client: GeminiClient,
        secrets: SecureApiKeyStore,
        model: String,
        thinking: String,
        onStage: suspend (String) -> Unit
    ): String {
        val key = secrets.getSecret("gemini_agent_key_5") ?: return ""
        return try {
            onStage("Hata ayıklama ajanı yanıtı inceliyor")
            val reviewChat = chat.copy(messages = chat.messages + ChatMessage(role = "assistant", content = answer) + ChatMessage(
                role = "user",
                content = "Önceki yanıtı doğruluk, eksik gereksinimler, güvenlik ve yanlış başarı iddiaları açısından denetle. Gizli düşünce zinciri verme. Yalnızca gerçek bir düzeltme gerekiyorsa en fazla 5 maddelik düzeltme listesi yaz; sorun yoksa yalnızca APPROVED yaz.\nYanıt:\n${answer.take(6000)}"
            ))
            val result = client.reply(reviewChat, key, model, false, thinking).text.trim()
            if (result.isBlank() || result.equals("APPROVED", true)) "" else result.take(1800)
        } catch (_: Exception) { "" }
    }
}
