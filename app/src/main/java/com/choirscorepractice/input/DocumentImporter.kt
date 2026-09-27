package com.choirscorepractice.input

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import com.choirscorepractice.core.pronunciation.PronunciationText
import java.io.IOException
import com.choirscorepractice.pdf.PdfDocumentSession

/** Blocking provider operations; call only from an I/O dispatcher. */
class DocumentImporter(private val resolver: ContentResolver) {
    fun selectPdf(uri: Uri): SelectedPdf {
        val pages = PdfDocumentSession.openRenderer(resolver, uri).use { document ->
            require(document.pageCount > 0) { "Empty PDF" }
            document.openPage(0).use { page ->
                require(page.width > 0 && page.height > 0) { "Invalid page size" }
            }
            document.pageCount
        }
        return SelectedPdf(uri, displayName(uri), pages)
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

data class SelectedPdf(
    val uri: Uri,
    val name: String?,
    val pageCount: Int,
    val persisted: Boolean = false,
)
data class ImportedText(val name: String?, val text: String)
