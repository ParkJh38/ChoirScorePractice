package com.choirscorepractice.core.pronunciation

import java.nio.charset.CharacterCodingException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PronunciationTextTest {
    @Test fun `preserves Korean composed and decomposed characters and whitespace exactly`() {
        val text = "\uFEFF  한글 한글 ㄱㅏ\r\n발음\n\n𝄞 🎵\t \r"
        assertEquals(text, PronunciationText.readUtf8(text.byteInputStream()))
    }

    @Test fun `accepts empty text and exact byte limit`() {
        assertEquals("", PronunciationText.readUtf8(byteArrayOf().inputStream()))
        val text = "a".repeat(PronunciationText.MAX_IMPORT_BYTES)
        assertEquals(text, PronunciationText.readUtf8(text.byteInputStream()))
    }

    @Test fun `rejects oversized input rather than truncating`() {
        val bytes = ByteArray(PronunciationText.MAX_IMPORT_BYTES + 1) { 65 }
        assertThrows(IllegalArgumentException::class.java) {
            PronunciationText.readUtf8(bytes.inputStream())
        }
    }

    @Test fun `rejects malformed UTF-8 rather than replacing characters`() {
        for (bytes in listOf(byteArrayOf(0xC3.toByte(), 0x28), byteArrayOf(0xED.toByte(), 0x95.toByte()))) {
            assertThrows(CharacterCodingException::class.java) {
                PronunciationText.readUtf8(bytes.inputStream())
            }
        }
    }
}
