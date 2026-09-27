package com.choirscorepractice.pdf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.choirscorepractice.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** No bitmap cache: only attached rows request/retain images. Detached rows drop all bitmap references. */
class PdfPageAdapter(
    private val model: PdfViewerViewModel,
    private val scope: CoroutineScope,
) : RecyclerView.Adapter<PdfPageAdapter.PageHolder>() {
    private var info: PdfInfo? = null
    private var pageWidth = 1

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    fun configure(document: PdfInfo, width: Int) {
        if (document == info && width == pageWidth) return
        val oldCount = itemCount
        info = document
        pageWidth = width
        if (oldCount != itemCount) {
            if (oldCount > 0) notifyItemRangeRemoved(0, oldCount)
            notifyItemRangeInserted(0, itemCount)
        } else {
            notifyItemRangeChanged(0, itemCount)
        }
    }

    override fun getItemCount() = info?.pageCount ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder =
        PageHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_pdf_page, parent, false))

    override fun onBindViewHolder(holder: PageHolder, position: Int) {
        holder.release()
        holder.page = position
        val document = checkNotNull(info)
        holder.label.text = holder.itemView.context.getString(R.string.pdf_page_number, position + 1)
        holder.image.contentDescription = holder.label.text
        holder.resize(document.firstPageWidth, document.firstPageHeight)
        if (holder.itemView.isAttachedToWindow) holder.render()
    }

    override fun onViewAttachedToWindow(holder: PageHolder) = holder.render()
    override fun onViewDetachedFromWindow(holder: PageHolder) = holder.release()
    override fun onViewRecycled(holder: PageHolder) = holder.release()

    inner class PageHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.page_label)
        val image: ImageView = view.findViewById(R.id.page_image)
        private val frame: View = view.findViewById(R.id.page_frame)
        private val progress: ProgressBar = view.findViewById(R.id.page_progress)
        private val retry: Button = view.findViewById(R.id.page_retry)
        private var job: Job? = null
        var page = 0

        init {
            retry.setOnClickListener { render() }
        }

        fun resize(width: Int, height: Int) {
            frame.layoutParams = frame.layoutParams.apply {
                this.height = (pageWidth.toDouble() * height / width).toInt().coerceIn(1, 32768)
            }
        }

        fun render() {
            if (job?.isActive == true || image.drawable != null) return
            progress.visibility = View.VISIBLE
            retry.visibility = View.GONE
            job = scope.launch {
                try {
                    val bitmap = model.render(page, pageWidth)
                    resize(bitmap.width, bitmap.height)
                    image.setImageBitmap(bitmap)
                    progress.visibility = View.GONE
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    progress.visibility = View.GONE
                    retry.visibility = View.VISIBLE
                }
            }
        }

        fun release() {
            job?.cancel()
            job = null
            image.setImageDrawable(null)
            // Let Android release bitmaps safely after pending display-list drawing finishes.
            progress.visibility = View.GONE
            retry.visibility = View.GONE
        }
    }
}
