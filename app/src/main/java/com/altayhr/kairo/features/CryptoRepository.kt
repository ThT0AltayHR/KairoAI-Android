package com.altayhr.kairo.features

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

data class CryptoQuote(
    val symbol: String,
    val price: Double,
    val change24h: Double,
    val currency: String,
    val updatedAt: Long = System.currentTimeMillis()
)

/** Read-only quote display. It does not place orders and never recommends buying or selling. */
object CryptoRepository {
    private val supportedSymbols = listOf("BTC", "ETH", "SOL", "ADA", "DOGE", "XRP")
    fun symbols() = supportedSymbols

    suspend fun fetch(symbol: String, currency: String): CryptoQuote? = withContext(Dispatchers.IO) {
        val coin = symbol.uppercase(Locale.ROOT)
        val fiat = currency.uppercase(Locale.ROOT)
        if (coin !in supportedSymbols || fiat !in setOf("USD", "EUR")) return@withContext null
        val connection = (URL("https://api.exchange.coinbase.com/products/$coin-$fiat/stats").openConnection() as HttpURLConnection).apply {
            connectTimeout = 9000; readTimeout = 9000; requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "KairoAI-Android/0.2")
        }
        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            val last = json.optString("last").toDoubleOrNull() ?: return@withContext null
            val open = json.optString("open").toDoubleOrNull() ?: last
            if (last <= 0) return@withContext null
            CryptoQuote(coin, last, if (open > 0) ((last - open) / open) * 100.0 else 0.0, fiat)
        } catch (_: Exception) { null }
        finally { connection.disconnect() }
    }

    fun formatPrice(price: Double, currency: String): String = runCatching {
        NumberFormat.getCurrencyInstance(Locale.US).apply { this.currency = Currency.getInstance(currency) }.format(price)
    }.getOrElse { "${"%.4f".format(Locale.US, price)} $currency" }
}
