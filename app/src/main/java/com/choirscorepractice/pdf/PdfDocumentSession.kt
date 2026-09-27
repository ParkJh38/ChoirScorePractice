package com.choirscorepractice.pdf

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.core.graphics.createBitmap
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.sqrt

/** Owns one renderer/descriptor. All native access, including close, is serialized off the UI thread. */
class PdfDocumentSession(private val resolver: ContentResolver, private val uri: Uri) {
    private val mutex = Mutex()
    private var renderer: PdfRenderer? = null
    private var closed = false

    suspend fun open(): PdfInfo = withContext(Dispatchers.IO) {
        mutex.withLock {
            check(!closed)
            currentCoroutineContext().ensureActive()
            val document = renderer ?: openRenderer(resolver, uri).also { renderer = it }
            require(document.pageCount > 0) { "Empty PDF" }
            document.openPage(0).use { page ->
                PdfInfo(document.pageCount, page.width, page.height)
            }
        }
    }

    suspend fun render(pageIndex: Int, requestedWidth: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            currentCoroutineContext().ensureActive()
            check(!closed)
            val document = checkNotNull(renderer)
            document.openPage(pageIndex).use { page ->
                val size = renderSize(page.width, page.height, requestedWidth)
                val bitmap = createBitmap(size.first, size.second, Bitmap.Config.ARGB_8888)
                try {
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    currentCoroutineContext().ensureActive()
                    bitmap
                } catch (failure: Throwable) {
                    bitmap.recycle()
                    throw failure
                }
            }
        }
    }

    /** Not tied to a destroyed Activity scope: a native render may still be completing. */
    fun closeAsync() {
        CoroutineScope(Dispatchers.IO).launch {
            mutex.withLock {
                closed = true
                renderer?.close()
                renderer = null
            }
        }
    }

    companion object {
        const val MAX_PAGE_PIXELS = 2_000_000
        const val MAX_BITMAP_SIDE = 4096

        fun renderSize(width: Int, height: Int, requestedWidth: Int): Pair<Int, Int> {
            require(width > 0 && height > 0 && requestedWidth > 0)
            val scale = min(
                requestedWidth.toDouble() / width,
                min(sqrt(MAX_PAGE_PIXELS.toDouble() / width / height), MAX_BITMAP_SIDE.toDouble() / maxOf(width, height)),
            )
            return maxOf(1, (width * scale).toInt()) to maxOf(1, (height * scale).toInt())
        }

        /** PdfRenderer takes ownership of the descriptor only after successful construction. */
        fun openRenderer(resolver: ContentResolver, uri: Uri): PdfRenderer {
            val descriptor = resolver.openFileDescriptor(uri, "r") ?: throw IOException("Cannot open PDF")
            try {
                return PdfRenderer(descriptor)
            } catch (failure: Throwable) {
                descriptor.close()
                throw failure
            }
        }
    }
}

data class PdfInfo(val pageCount: Int, val firstPageWidth: Int, val firstPageHeight: Int)
