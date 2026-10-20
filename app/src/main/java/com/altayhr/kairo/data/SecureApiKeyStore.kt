package com.altayhr.kairo.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts the user-provided API key with an AES key held in Android Keystore. */
class SecureApiKeyStore(context: Context) {
    private val prefs = context.getSharedPreferences("kairo_secure_settings", Context.MODE_PRIVATE)
    private val alias = "kairo_api_key_aes_v1"

    fun saveApiKey(value: String) = saveSecret("gemini_api_key", value)

    fun getApiKey(): String? = getSecret("gemini_api_key")

    /** Encrypts any integration credential at rest using the same device-bound Android Keystore key. */
    fun saveSecret(name: String, value: String) {
        val keyName = safeKeyName(name)
        if (value.isBlank()) { clearSecret(name); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(value.trim().toByteArray(Charsets.UTF_8))
        prefs.edit().putString("${keyName}_iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString("${keyName}_ciphertext", Base64.encodeToString(ciphertext, Base64.NO_WRAP)).apply()
    }

    fun getSecret(name: String): String? = runCatching {
        val keyName = safeKeyName(name)
        val iv = prefs.getString("${keyName}_iv", null) ?: return null
        val encrypted = prefs.getString("${keyName}_ciphertext", null) ?: return null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()

    fun clearSecret(name: String) {
        val keyName = safeKeyName(name)
        prefs.edit().remove("${keyName}_iv").remove("${keyName}_ciphertext").apply()
    }

    fun clearApiKey() = clearSecret("gemini_api_key")
    fun model(): String = prefs.getString("model", "gemini-4-argon") ?: "gemini-4-argon"
    fun saveModel(model: String) { prefs.edit().putString("model", model.trim().ifBlank { "gemini-4-argon" }).apply() }
    fun thinkingLevel(): String = prefs.getString("thinking_level", "high") ?: "high"
    fun saveThinkingLevel(value: String) { prefs.edit().putString("thinking_level", value.lowercase().takeIf { it in setOf("low", "medium", "high") } ?: "high").apply() }
    fun liveModel(): String = prefs.getString("live_model", "gemini-3.8-live") ?: "gemini-3.8-live"
    fun saveLiveModel(value: String) { prefs.edit().putString("live_model", value.trim().ifBlank { "gemini-3.8-live" }).apply() }
    fun liveVoice(): String = prefs.getString("live_voice", "Kore") ?: "Kore"
    fun saveLiveVoice(value: String) { prefs.edit().putString("live_voice", value.takeIf { it in setOf("Aoede", "Charon", "Kore", "Fenrir", "Puck", "Leda", "Orus", "Zephyr") } ?: "Kore").apply() }

    private fun safeKeyName(name: String) = "secret_" + name.lowercase().replace(Regex("[^a-z0-9_]+"), "_").take(60)

    private fun getOrCreateKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true).build())
        return generator.generateKey()
    }
}
