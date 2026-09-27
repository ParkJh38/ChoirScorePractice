package com.choirscorepractice.pdf

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PdfViewerState(val info: PdfInfo? = null, val failed: Boolean = false)

class PdfViewerViewModel(application: Application) : AndroidViewModel(application) {
    private var session: PdfDocumentSession? = null
    private val mutableState = MutableStateFlow(PdfViewerState())
    val state = mutableState.asStateFlow()

    fun open(uri: Uri?) {
        if (session != null) return
        if (uri == null) {
            mutableState.value = PdfViewerState(failed = true)
            return
        }
        val document = PdfDocumentSession(getApplication<Application>().contentResolver, uri)
        session = document
        viewModelScope.launch {
            try {
                mutableState.value = PdfViewerState(info = document.open())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                document.closeAsync()
                mutableState.value = PdfViewerState(failed = true)
            }
        }
    }

    suspend fun render(page: Int, width: Int): Bitmap = checkNotNull(session).render(page, width)

    override fun onCleared() {
        session?.closeAsync()
    }
}
