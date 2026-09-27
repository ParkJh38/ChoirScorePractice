package com.choirscorepractice.core.pronunciation

import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Strict UTF-8: preserves every code point, including a BOM, whitespace and line endings. */
object PronunciationText {
    const val MAX_IMPORT_BYTES = 64 * 1024

    /** The caller owns and closes [input]. Reads at most one byte beyond the limit. */
    fun readUtf8(input: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (output.size() <= MAX_IMPORT_BYTES) {
            val count = input.read(buffer, 0, minOf(buffer.size, MAX_IMPORT_BYTES + 1 - output.size()))
            if (count < 0) break
            output.write(buffer, 0, count)
        }
        if (output.size() > MAX_IMPORT_BYTES) throw PronunciationTooLargeException()
        val bytes = output.toByteArray()
        return Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes))
            .toString()
    }
}

class PronunciationTooLargeException : IllegalArgumentException("Pronunciation file exceeds 64 KiB")
