package com.choirscorepractice

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.choirscorepractice.pdf.PdfDocumentSession
import com.choirscorepractice.pdf.PdfViewerActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfViewerTest {
    private var scenario: ActivityScenario<PdfViewerActivity>? = null

    @After fun close() { scenario?.close() }

    @Test fun scrollsMultiPageScoreWithBoundedBitmapsAndDistinctPageContent() {
        launch("many.pdf")
        awaitPage(0)
        checkPage(0, Color.BLUE)
        for (page in listOf(5, 12, 23)) {
            scenario!!.onActivity {
                (it.findViewById<RecyclerView>(R.id.pdf_pages).layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(page, 0)
            }
            awaitPage(page)
            checkPage(page, if (page % 2 == 0) Color.BLUE else Color.RED)
        }
        scenario!!.onActivity {
            val list = it.findViewById<RecyclerView>(R.id.pdf_pages)
            assertEquals(24, list.adapter!!.itemCount)
            assertTrue("Only visible rows should be attached", list.childCount < 5)
            assertEquals(null, list.findViewHolderForAdapterPosition(0))
        }
    }

    @Test fun zoomPansHorizontallyAndPreservesDocumentZoomAndPageAfterRecreation() {
        launch("many.pdf")
        awaitPage(0)
        scenario!!.onActivity { it.findViewById<Button>(R.id.zoom_in).performClick() }
        await { it.findViewById<TextView>(R.id.viewer_status).text == "24 pages · 150%" }
        awaitPage(0)
        scenario!!.onActivity {
            val scroll = it.findViewById<HorizontalScrollView>(R.id.pdf_horizontal_scroll)
            assertTrue(it.findViewById<RecyclerView>(R.id.pdf_pages).width > scroll.width)
            scroll.scrollTo(scroll.getChildAt(0).width, 0)
            assertTrue(scroll.scrollX > 0)
            (it.findViewById<RecyclerView>(R.id.pdf_pages).layoutManager as LinearLayoutManager)
                .scrollToPositionWithOffset(4, 0)
        }
        awaitPage(4)
        scenario!!.recreate()
        await { it.findViewById<TextView>(R.id.viewer_status).text == "24 pages · 150%" }
        awaitPage(4)
        scenario!!.onActivity {
            assertEquals("many.pdf", it.findViewById<TextView>(R.id.viewer_filename).text.toString())
            assertEquals(4, (it.findViewById<RecyclerView>(R.id.pdf_pages).layoutManager as LinearLayoutManager)
                .findFirstVisibleItemPosition())
            it.findViewById<Button>(R.id.zoom_fit).performClick()
        }
        await { it.findViewById<TextView>(R.id.viewer_status).text == "24 pages · 100%" }
        scenario!!.onActivity {
            assertEquals(0, it.findViewById<HorizontalScrollView>(R.id.pdf_horizontal_scroll).scrollX)
            assertTrue(!it.findViewById<Button>(R.id.zoom_out).isEnabled)
        }
    }

    @Test fun unavailablePdfShowsRecoveryAndLeavesZoomDisabled() {
        launch("missing.pdf")
        await { it.findViewById<View>(R.id.viewer_error).visibility == View.VISIBLE }
        scenario!!.onActivity {
            assertTrue(!it.findViewById<Button>(R.id.zoom_in).isEnabled)
            assertEquals(0, it.findViewById<RecyclerView>(R.id.pdf_pages).adapter!!.itemCount)
        }
    }

    @Test fun renderBudgetBoundsExtremePageSizes() {
        for ((width, height) in listOf(600 to 800, 800 to 600, 1 to Int.MAX_VALUE, Int.MAX_VALUE to 1, Int.MAX_VALUE to Int.MAX_VALUE)) {
            val (renderWidth, renderHeight) = PdfDocumentSession.renderSize(width, height, 10000)
            assertTrue(renderWidth in 1..PdfDocumentSession.MAX_BITMAP_SIDE)
            assertTrue(renderHeight in 1..PdfDocumentSession.MAX_BITMAP_SIDE)
            assertTrue(renderWidth.toLong() * renderHeight <= PdfDocumentSession.MAX_PAGE_PIXELS)
        }
    }

    private fun launch(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        scenario = ActivityScenario.launch(Intent(context, PdfViewerActivity::class.java).apply {
            data = FixtureProvider.uri(name)
            putExtra(PdfViewerActivity.EXTRA_NAME, name)
        })
    }

    private fun awaitPage(page: Int) = await {
        val holder = it.findViewById<RecyclerView>(R.id.pdf_pages).findViewHolderForAdapterPosition(page)
        holder?.itemView?.findViewById<ImageView>(R.id.page_image)?.drawable is BitmapDrawable
    }

    private fun checkPage(page: Int, expectedColor: Int) {
        scenario!!.onActivity {
            val holder = it.findViewById<RecyclerView>(R.id.pdf_pages).findViewHolderForAdapterPosition(page)
            assertNotNull(holder)
            val bitmap = (holder!!.itemView.findViewById<ImageView>(R.id.page_image).drawable as BitmapDrawable).bitmap
            assertTrue(bitmap.width.toLong() * bitmap.height <= PdfDocumentSession.MAX_PAGE_PIXELS)
            assertEquals(expectedColor, bitmap.getPixel(bitmap.width / 15, bitmap.height / 20))
        }
    }

    private fun await(predicate: (PdfViewerActivity) -> Boolean) {
        val deadline = System.nanoTime() + 10_000_000_000L
        var ready = false
        do {
            scenario!!.onActivity { ready = predicate(it) }
            if (ready) return
            Thread.sleep(25)
        } while (System.nanoTime() < deadline)
        assertTrue("Viewer condition was not met before timeout", ready)
    }
}
