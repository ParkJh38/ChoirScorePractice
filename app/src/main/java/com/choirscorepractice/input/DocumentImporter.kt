package com.choirscorepractice.input

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.choirscorepractice.core.pronunciation.PronunciationText
import java.io.IOException

/** Blocking provider operations; call only from an I/O dispatcher. */
class DocumentImporter(private val resolver: ContentResolver) {
    fun selectPdf(uri: Uri): SelectedPdf {
        // Verify access without reading a potentially large score into memory.
        resolver.openInputStream(uri)?.use { input ->
            val header = ByteArray(5)
            var count = 0
            while (count < header.size) {
                val read = input.read(header, count, header.size - count)
                if (read < 0) throw IOException("Missing PDF header")
                count += read
            }
            if (!header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))) {
                throw IOException("Missing PDF header")
            }
        } ?: throw IOException("Cannot open PDF")
        return SelectedPdf(uri, displayName(uri))
    }

    fun importText(uri: Uri): ImportedText {
        val text = resolver.openInputStream(uri)?.use(PronunciationText::readUtf8)
            ?: throw IOException("Cannot open pronunciation file")
        return ImportedText(displayName(uri), text)
    }

    private fun displayName(uri: Uri): String? =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst() && !it.isNull(0)) it.getString(0) else null
        }
}

data class SelectedPdf(val uri: Uri, val name: String?)
data class ImportedText(val name: String?, val text: String)
