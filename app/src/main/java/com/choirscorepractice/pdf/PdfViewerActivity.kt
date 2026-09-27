package com.choirscorepractice.pdf

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.choirscorepractice.R
import kotlinx.coroutines.launch

class PdfViewerActivity : ComponentActivity() {
    private val model: PdfViewerViewModel by viewModels()
    private lateinit var pages: RecyclerView
    private lateinit var horizontalScroll: HorizontalScrollView
    private lateinit var adapter: PdfPageAdapter
    private var zoomIndex = 0
    private var info: PdfInfo? = null
    private val zoomLevels = floatArrayOf(1f, 1.5f, 2f, 3f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        zoomIndex = (savedInstanceState?.getInt("zoom") ?: 0).coerceIn(zoomLevels.indices)
        pages = findViewById(R.id.pdf_pages)
        horizontalScroll = findViewById(R.id.pdf_horizontal_scroll)
        adapter = PdfPageAdapter(model, lifecycleScope)
        pages.layoutManager = LinearLayoutManager(this).apply { isItemPrefetchEnabled = false }
        pages.itemAnimator = null
        pages.setItemViewCacheSize(0)
        pages.adapter = adapter
        findViewById<TextView>(R.id.viewer_filename).text = intent.getStringExtra(EXTRA_NAME)
            ?: getString(R.string.unnamed_document)
        findViewById<Button>(R.id.close_viewer).setOnClickListener { finish() }
        findViewById<Button>(R.id.zoom_in).setOnClickListener { changeZoom(zoomIndex + 1) }
        findViewById<Button>(R.id.zoom_out).setOnClickListener { changeZoom(zoomIndex - 1) }
        findViewById<Button>(R.id.zoom_fit).setOnClickListener { changeZoom(0) }
        horizontalScroll.addOnLayoutChangeListener { _, left, _, right, _, oldLeft, _, oldRight, _ ->
            if (right - left != oldRight - oldLeft) configurePages()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.state.collect { state ->
                    info = state.info
                    findViewById<View>(R.id.viewer_loading).visibility =
                        if (state.info == null && !state.failed) View.VISIBLE else View.GONE
                    findViewById<View>(R.id.viewer_error).visibility = if (state.failed) View.VISIBLE else View.GONE
                    configurePages()
                }
            }
        }
        model.open(intent.data)
    }

    private fun changeZoom(index: Int) {
        val next = index.coerceIn(zoomLevels.indices)
        if (next == zoomIndex) return
        val layout = pages.layoutManager as LinearLayoutManager
        val first = layout.findFirstVisibleItemPosition().coerceAtLeast(0)
        val oldTop = layout.findViewByPosition(first)?.top ?: 0
        val ratio = zoomLevels[next] / zoomLevels[zoomIndex]
        zoomIndex = next
        configurePages()
        layout.scrollToPositionWithOffset(first, (oldTop * ratio).toInt())
        if (zoomIndex == 0) horizontalScroll.scrollTo(0, 0)
    }

    private fun configurePages() {
        val document = info
        findViewById<Button>(R.id.zoom_in).isEnabled = document != null && zoomIndex < zoomLevels.lastIndex
        findViewById<Button>(R.id.zoom_out).isEnabled = document != null && zoomIndex > 0
        findViewById<Button>(R.id.zoom_fit).isEnabled = document != null && zoomIndex > 0
        if (document == null || horizontalScroll.width == 0) return
        val width = (horizontalScroll.width * zoomLevels[zoomIndex]).toInt()
        if (pages.layoutParams.width != width) {
            pages.layoutParams = pages.layoutParams.apply { this.width = width }
        }
        adapter.configure(document, width)
        findViewById<TextView>(R.id.viewer_status).text = resources.getQuantityString(
            R.plurals.pdf_viewer_status, document.pageCount, document.pageCount, (zoomLevels[zoomIndex] * 100).toInt(),
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("zoom", zoomIndex)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        pages.adapter = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_NAME = "pdf_name"
    }
}
