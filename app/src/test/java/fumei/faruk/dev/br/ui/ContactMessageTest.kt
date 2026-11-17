package fumei.faruk.dev.br.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactMessageTest {
    @Test
    fun normalize_trimsAndNormalizesLineBreaks() {
        assertEquals("linha 1\nlinha 2\nlinha 3", ContactMessage.normalize("  linha 1\r\nlinha 2\rlinha 3  "))
        assertEquals("", ContactMessage.normalize("   "))
        assertEquals("oi\nfim", ContactMessage.normalize("oi\nfim"))
    }

    @Test
    fun isValid_requiresThreeCharactersAfterTrim() {
        assertFalse(ContactMessage.isValid(""))
        assertFalse(ContactMessage.isValid("  a  "))
        assertFalse(ContactMessage.isValid("oi"))
        assertFalse(ContactMessage.isValid("   oi   "))
        assertTrue(ContactMessage.isValid("oi!"))
        assertTrue(ContactMessage.isValid("  oi!  "))
    }

    @Test
    fun limit_keepsAtMostTwoThousandCharacters() {
        val long = "a".repeat(ContactMessage.MAX_LENGTH + 1)
        assertEquals(ContactMessage.MAX_LENGTH, ContactMessage.limit(long).length)
        assertEquals(ContactMessage.MAX_LENGTH + 1, long.length)
        assertEquals("curto", ContactMessage.limit("curto"))
    }

    @Test
    fun body_withoutDetailsHasNoTrailingSeparator() {
        assertEquals("oi", ContactMessage.body("oi", emptyList()))
        assertEquals("oi", ContactMessage.body("  oi  "))
    }

    @Test
    fun body_appendsDetailsAfterSeparator() {
        val details = listOf("Fumei 1.4.0 (24)", "Android 14 · Pixel 6", "pt-BR")
        val body = ContactMessage.body("oi", details)

        assertTrue(body.startsWith("oi"))
        assertTrue(
            body.endsWith("\n\n-- \nFumei 1.4.0 (24)\nAndroid 14 · Pixel 6\npt-BR"),
        )
    }

    @Test
    fun mailto_encodesSubjectAndBody() {
        assertEquals(
            "mailto:a@b.c?subject=Oi%20%C3%A7%C3%A3o&body=linha%201%0Alinha%202",
            ContactMessage.mailto("a@b.c", "Oi ção", "linha 1\nlinha 2"),
        )
    }

    @Test
    fun encode_usesPercentTwentyForSpaces() {
        val encoded = ContactMessage.encode("a b")

        assertEquals("a%20b", encoded)
        assertFalse(encoded.contains("+"))
    }
}
