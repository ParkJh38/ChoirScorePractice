package com.choirscorepractice

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.choirscorepractice.input.InputError
import com.choirscorepractice.input.InputState
import com.choirscorepractice.input.InputViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val model: InputViewModel by viewModels()
    private lateinit var pronunciation: EditText
    private var rendering = false

    private val pdfPicker = registerForActivityResult(LocalDocument()) { uri ->
        uri?.let(model::selectPdf)
    }
    private val txtPicker = registerForActivityResult(LocalDocument()) { uri ->
        uri?.let(model::importText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pronunciation = findViewById(R.id.pronunciation)
        pronunciation.doAfterTextChanged {
            if (!rendering) model.editPronunciation(it.toString())
        }
        findViewById<Button>(R.id.select_pdf).setOnClickListener {
            launchPicker { pdfPicker.launch(arrayOf("application/pdf")) }
        }
        findViewById<Button>(R.id.import_txt).setOnClickListener {
            launchPicker { txtPicker.launch(arrayOf("text/plain")) }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect(::render)
            }
        }
    }

    private fun launchPicker(launch: () -> Unit) {
        try {
            launch()
        } catch (_: ActivityNotFoundException) {
            model.pickerUnavailable()
        }
    }

    private fun render(state: InputState) {
        findViewById<TextView>(R.id.pdf_filename).text = state.pdf?.let {
            getString(R.string.pdf_filename, it.name ?: getString(R.string.unnamed_document))
        } ?: getString(R.string.no_pdf)
        findViewById<TextView>(R.id.txt_filename).text = if (state.txtSelected) {
            getString(R.string.txt_filename, state.txtName ?: getString(R.string.unnamed_document))
        } else {
            getString(R.string.no_txt)
        }
        if (pronunciation.text.toString() != state.pronunciation) {
            rendering = true
            pronunciation.setText(state.pronunciation)
            pronunciation.setSelection(pronunciation.length())
            rendering = false
        }
        pronunciation.isEnabled = !state.busy
        findViewById<Button>(R.id.select_pdf).isEnabled = !state.busy
        findViewById<Button>(R.id.import_txt).isEnabled = !state.busy
        findViewById<View>(R.id.import_progress).visibility = if (state.busy) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.input_error).apply {
            text = state.error?.let { getString(it.messageResource()) }.orEmpty()
            visibility = if (state.error == null) View.GONE else View.VISIBLE
        }
    }

    private fun InputError.messageResource(): Int = when (this) {
        InputError.PDF -> R.string.pdf_error
        InputError.TEXT -> R.string.txt_error
        InputError.ENCODING -> R.string.encoding_error
        InputError.SIZE -> R.string.size_error
        InputError.PICKER -> R.string.picker_error
    }
}

/** Ask SAF for local files only. No broad storage permission or remote service is needed. */
private class LocalDocument : ActivityResultContracts.OpenDocument() {
    override fun createIntent(context: Context, input: Array<String>): Intent =
        super.createIntent(context, input).putExtra(Intent.EXTRA_LOCAL_ONLY, true)
}
