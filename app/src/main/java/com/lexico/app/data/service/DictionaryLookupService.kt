package com.lexico.app.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class LookupResult(
    val translation: String,
    val exampleSentence: String? = null
)

object DictionaryLookupService {

    private val offlineDictionary = mapOf(
        "ineffable" to Pair("Inefable, que no puede expresarse con palabras", "There was an ineffable melancholy in the quiet room as twilight faded."),
        "serendipity" to Pair("Hallazgo afortunado e inesperado", "Finding that rare first edition in a dusty bookstore was pure serendipity."),
        "machiavellian" to Pair("Maquiavélico, calculador y sin escrúpulos", "His Machiavellian maneuvers kept everyone guessing his next move."),
        "ephemeral" to Pair("Efímero, fugaz, de corta duración", "The ephemeral bloom of cherry blossoms lasted only a few days."),
        "resilience" to Pair("Capacidad de sobreponerse y resistir", "The staff demonstrated incredible resilience through every crisis."),
        "ubiquitous" to Pair("Presente en todas partes a la vez", "Smartphones have become ubiquitous in modern daily life."),
        "ruthless" to Pair("Despiadado, implacable", "The desert climate proved ruthless to those who traveled unprepared."),
        "dystopian" to Pair("Distópico, sociedad opresiva y deshumanizada", "The novel portrays a grim dystopian regime under total surveillance."),
        "solace" to Pair("Consuelo, alivio o sosiego", "She found solace in reading classical literature during long evenings."),
        "eloquent" to Pair("Elocuente, persuasivo y fluido al expresarse", "His eloquent speech moved the entire audience to reflection."),
        "pristine" to Pair("Prístino, intacto y en su pureza original", "The mountain lake remained untouched in its pristine beauty."),
        "meticulous" to Pair("Meticuloso, detallista y muy cuidadoso", "The artisan was meticulous in polishing every single edge."),
        "luminous" to Pair("Luminoso, resplandeciente o radiante", "A luminous dawn broke softly over the coastal horizon."),
        "enigma" to Pair("Enigma, misterio difícil de descifrar", "Her sudden departure remained an unsolved enigma for years."),
        "tenacity" to Pair("Tenacidad, perseverancia inquebrantable", "His quiet tenacity allowed him to master difficult literary texts."),
        "catharsis" to Pair("Catarsis, purificación y liberación emocional", "Writing the concluding chapter brought an unexpected sense of catharsis."),
        "lucid" to Pair("Lúcido, claro, inteligible y perspicaz", "She offered a lucid and insightful explanation of the theory."),
        "sonorous" to Pair("Sonoro, profundo y resonante", "The sonorous chime of the bell echoed through the valley.")
    )

    suspend fun lookupWord(word: String): Result<LookupResult> = withContext(Dispatchers.IO) {
        val cleanWord = word.trim().lowercase()
        if (cleanWord.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Palabra vacía"))
        }

        // Check offline dictionary first as instant candidate
        val offline = offlineDictionary[cleanWord]

        var onlineTranslation: String? = null
        var onlineExample: String? = null

        // 1. Fetch translation from MyMemory API
        try {
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = URL("https://api.mymemory.translated.net/get?q=$encoded&langpair=en|es")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "LexicoApp/1.0")
            }
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val responseData = root.optJSONObject("responseData")
                val rawTrans = responseData?.optString("translatedText")
                if (!rawTrans.isNullOrBlank() && !rawTrans.equals(cleanWord, ignoreCase = true)) {
                    onlineTranslation = unescapeHtml(rawTrans.trim().replaceFirstChar { it.uppercase() })
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
            // Fallback gracefully
        }

        // 2. Fetch definition and example from Free Dictionary API
        try {
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = URL("https://api.dictionaryapi.dev/api/v2/entries/en/$encoded")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3500
                readTimeout = 3500
                requestMethod = "GET"
                setRequestProperty("User-Agent", "LexicoApp/1.0")
            }
            if (conn.responseCode == 200) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val rootArray = JSONArray(jsonStr)
                if (rootArray.length() > 0) {
                    val entry = rootArray.getJSONObject(0)
                    val meanings = entry.optJSONArray("meanings")
                    if (meanings != null) {
                        for (i in 0 until meanings.length()) {
                            val meaning = meanings.getJSONObject(i)
                            val definitions = meaning.optJSONArray("definitions") ?: continue
                            for (j in 0 until definitions.length()) {
                                val defObj = definitions.getJSONObject(j)
                                val example = defObj.optString("example")
                                if (!example.isNullOrBlank() && onlineExample == null) {
                                    onlineExample = example.trim().replaceFirstChar { it.uppercase() }
                                }
                            }
                            if (onlineExample != null) break
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
            // Fallback gracefully
        }

        val finalTranslation = onlineTranslation ?: offline?.first
        val finalExample = onlineExample ?: offline?.second

        if (finalTranslation != null) {
            Result.success(LookupResult(translation = finalTranslation, exampleSentence = finalExample))
        } else if (offline != null) {
            Result.success(LookupResult(translation = offline.first, exampleSentence = offline.second))
        } else {
            Result.failure(Exception("No se encontró traducción online ni offline."))
        }
    }

    private fun unescapeHtml(text: String): String {
        return text
            .replace("&#39;", "'")
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&#039;", "'")
    }
}
