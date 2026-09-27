package com.choirscorepractice.input

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Stores only the last PDF URI; user documents stay with their local SAF provider. Call on I/O. */
@SuppressLint("UseKtx") // Need commit() success to decide whether the old URI grant may be released.
class SelectedPdfStore(context: Context) {
    private val resolver = context.contentResolver
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun savedUri(): Uri? = preferences.getString("uri", null)?.let(Uri::parse)

    fun hasPersistedAccess(uri: Uri): Boolean =
        resolver.persistedUriPermissions.any { it.uri == uri && it.isReadPermission }

    fun remember(pdf: SelectedPdf): SelectedPdf {
        val previous = savedUri()
        val persisted = try {
            resolver.takePersistableUriPermission(pdf.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            true
        } catch (_: SecurityException) {
            false // Some providers grant only session access. Keep viewing available and disclose it.
        }
        val stored = preferences.edit().apply {
            if (persisted) putString("uri", pdf.uri.toString()) else remove("uri")
        }.commit()
        if (!stored) {
            if (pdf.uri != previous) release(pdf.uri)
            return pdf.copy(persisted = false)
        }
        if (previous != null && previous != pdf.uri) release(previous)
        return pdf.copy(persisted = persisted)
    }

    fun forget() {
        val previous = savedUri()
        if (preferences.edit().clear().commit() && previous != null) release(previous)
    }

    private fun release(uri: Uri) {
        try {
            resolver.releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // Already revoked, or a provider that never granted persistence.
        }
    }

    companion object {
        const val PREFERENCES = "selected_pdf"
    }
}
