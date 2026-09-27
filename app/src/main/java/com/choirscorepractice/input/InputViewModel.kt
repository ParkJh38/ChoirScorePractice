package com.choirscorepractice.input

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.choirscorepractice.core.pronunciation.PronunciationTooLargeException
import java.io.IOException
import java.nio.charset.CharacterCodingException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class InputError { PDF, TEXT, ENCODING, SIZE, PICKER }

data class InputState(
    val pdf: SelectedPdf? = null,
    val txtSelected: Boolean = false,
    val txtName: String? = null,
    val pronunciation: String = "",
    val busy: Boolean = false,
    val error: InputError? = null,
)

/** Session state survives rotation. No large document/text payloads go into saved-state Bundles. */
class InputViewModel(application: Application) : AndroidViewModel(application) {
    private val importer = DocumentImporter(application.contentResolver)
    private val mutableState = MutableStateFlow(InputState())
    val state = mutableState.asStateFlow()

    fun editPronunciation(text: String) {
        if (!mutableState.value.busy) {
            mutableState.value = mutableState.value.copy(pronunciation = text, error = null)
        }
    }

    fun pickerUnavailable() {
        mutableState.value = mutableState.value.copy(error = InputError.PICKER)
    }

    fun selectPdf(uri: Uri) = importDocument(InputError.PDF) {
        val pdf = withContext(Dispatchers.IO) { importer.selectPdf(uri) }
        mutableState.value = mutableState.value.copy(pdf = pdf)
    }

    fun importText(uri: Uri) = importDocument(InputError.TEXT) {
        val imported = withContext(Dispatchers.IO) { importer.importText(uri) }
        mutableState.value = mutableState.value.copy(
            txtSelected = true,
            txtName = imported.name,
            pronunciation = imported.text,
        )
    }

    private fun importDocument(fallback: InputError, operation: suspend () -> Unit) {
        if (mutableState.value.busy) return
        mutableState.value = mutableState.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try {
                operation()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: CharacterCodingException) {
                fail(InputError.ENCODING)
            } catch (_: PronunciationTooLargeException) {
                fail(InputError.SIZE)
            } catch (_: IllegalArgumentException) {
                fail(fallback)
            } catch (_: IOException) {
                fail(fallback)
            } catch (_: SecurityException) {
                fail(fallback)
            } finally {
                mutableState.value = mutableState.value.copy(busy = false)
            }
        }
    }

    private fun fail(error: InputError) {
        mutableState.value = mutableState.value.copy(error = error)
    }
}
