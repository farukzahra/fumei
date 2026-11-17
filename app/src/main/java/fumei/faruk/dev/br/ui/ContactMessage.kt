package fumei.faruk.dev.br.ui

import java.nio.charset.StandardCharsets

object ContactMessage {
    const val MAX_LENGTH = 2000
    const val MIN_LENGTH = 3
    const val DETAILS_SEPARATOR = "\n\n-- \n"

    private const val HEX = "0123456789ABCDEF"

    fun normalize(raw: String): String =
        raw.replace("\r\n", "\n").replace('\r', '\n').trim()

    fun isValid(raw: String): Boolean = normalize(raw).length >= MIN_LENGTH

    fun limit(raw: String): String = raw.take(MAX_LENGTH)

    fun body(message: String, header: List<String> = emptyList()): String {
        val text = normalize(message)
        val details = header.map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")
        return if (details.isEmpty()) text else text + DETAILS_SEPARATOR + details
    }

    fun mailto(email: String, subject: String, body: String): String =
        "mailto:${email.trim()}?subject=${encode(subject)}&body=${encode(body)}"

    fun encode(value: String): String {
        val out = StringBuilder()
        value.toByteArray(StandardCharsets.UTF_8).forEach { byte ->
            val code = byte.toInt() and 0xFF
            if (code < 0x80 && isUnreserved(code.toChar())) {
                out.append(code.toChar())
            } else {
                out.append('%')
                out.append(HEX[code shr 4])
                out.append(HEX[code and 0x0F])
            }
        }
        return out.toString()
    }

    private fun isUnreserved(char: Char): Boolean =
        char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~"
}
