package fumei.faruk.dev.br.ui

import android.os.Build
import fumei.faruk.dev.br.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

const val WEB3FORMS_ENDPOINT = "https://api.web3forms.com/submit"

// Chave pública por design (vai dentro do APK). Fonte: secrets/projects/web3forms.env.
// Rotacionar em web3forms.com se o formulário receber spam.
const val WEB3FORMS_ACCESS_KEY = "80f793bf-bf27-47e2-963b-87032fc75f4a"

interface ContactSender {
    suspend fun send(message: String): Boolean
}

object ContactPayload {
    const val FROM_NAME = "Fumei"

    fun json(
        accessKey: String,
        subject: String,
        message: String,
        fromName: String = FROM_NAME,
        fields: Map<String, String> = emptyMap(),
    ): String {
        val parts = mutableListOf(
            "\"access_key\":\"${escape(accessKey)}\"",
            "\"subject\":\"${escape(subject)}\"",
            "\"from_name\":\"${escape(fromName)}\"",
            "\"message\":\"${escape(message)}\"",
        )
        fields.forEach { (key, value) ->
            parts += "\"${escape(key)}\":\"${escape(value)}\""
        }
        parts += "\"botcheck\":false"
        return parts.joinToString(separator = ",", prefix = "{", postfix = "}")
    }

    private fun escape(value: String): String {
        val out = StringBuilder(value.length + 16)
        value.forEach { char ->
            when (char) {
                '\\' -> out.append("\\\\")
                '"' -> out.append("\\\"")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\u000C' -> out.append("\\f")
                else -> if (char < ' ') {
                    out.append("\\u")
                    out.append(char.code.toString(16).padStart(4, '0'))
                } else {
                    out.append(char)
                }
            }
        }
        return out.toString()
    }
}

class Web3FormsSender(
    private val accessKey: String = WEB3FORMS_ACCESS_KEY,
    private val endpoint: String = WEB3FORMS_ENDPOINT,
) : ContactSender {
    override suspend fun send(message: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val payload = ContactPayload.json(
                accessKey = accessKey,
                subject = "Fumei ${BuildConfig.VERSION_NAME} · sugestão",
                message = message,
                fields = mapOf(
                    "app" to "Fumei",
                    "version" to "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    "android" to "Android ${Build.VERSION.RELEASE}",
                    "device" to Build.MODEL,
                    "language" to Locale.getDefault().toLanguageTag(),
                ),
            )
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 10_000
                connection.readTimeout = 15_000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("User-Agent", "Fumei/${BuildConfig.VERSION_NAME} (Android)")
                connection.outputStream.use { stream ->
                    stream.write(payload.toByteArray(Charsets.UTF_8))
                }
                if (connection.responseCode !in 200..299) return@runCatching false
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                body.contains("\"success\":true") || body.contains("\"success\": true")
            } finally {
                connection.disconnect()
            }
        }.getOrDefault(false)
    }
}
