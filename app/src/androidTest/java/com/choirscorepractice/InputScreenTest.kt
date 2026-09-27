package com.choirscorepractice

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import androidx.test.platform.app.InstrumentationRegistry
import com.choirscorepractice.input.SelectedPdfStore
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.arrayContaining
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class InputScreenTest {
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before fun launch() {
        SelectedPdfStore(InstrumentationRegistry.getInstrumentation().targetContext).forget()
        Intents.init()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        awaitReady()
    }

    @After fun close() {
        scenario.close()
        SelectedPdfStore(InstrumentationRegistry.getInstrumentation().targetContext).forget()
        Intents.release()
    }

    @Test fun importedFilenamesAndKoreanSurviveRotationAndRemainEditable() {
        pick(R.id.select_pdf, "application/pdf", "합창.pdf")
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
        pick(R.id.import_txt, "text/plain", "발음.txt")
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
        awaitText(R.id.txt_filename, "TXT: 발음.txt")
        scenario.recreate()
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
        awaitText(R.id.txt_filename, "TXT: 발음.txt")
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
        val edited = FixtureProvider.KOREAN + "\n직접 입력 가"
        scenario.onActivity { it.findViewById<EditText>(R.id.pronunciation).append("\n직접 입력 가") }
        scenario.recreate()
        awaitText(R.id.pronunciation, edited)
    }

    @Test fun directKoreanInputAndCancelledPickerPreserveText() {
        val text = "  직접 입력\n가 한글 🎵\n"
        scenario.onActivity { activity ->
            val editor = activity.findViewById<EditText>(R.id.pronunciation)
            editor.setText("  직접 입력\n")
            editor.requestFocus()
            editor.setSelection(editor.length())
            activity.getSystemService(ClipboardManager::class.java)
                .setPrimaryClip(ClipData.newPlainText("Pronunciation test", "가 한글 🎵\n"))
            editor.onTextContextMenuItem(android.R.id.paste)
        }
        awaitText(R.id.pronunciation, text)
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(ActivityResult(Activity.RESULT_CANCELED, null))
        onView(withId(R.id.import_txt)).perform(scrollTo(), click())
        scenario.recreate()
        awaitText(R.id.pronunciation, text)
        awaitText(R.id.txt_filename, "No TXT imported")
    }

    @Test fun invalidUtf8AndOversizedImportPreservePreviousTextAndFilename() {
        pick(R.id.import_txt, "text/plain", "발음.txt")
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
        pick(R.id.import_txt, "text/plain", "invalid.txt")
        awaitText(R.id.input_error, "This TXT is not valid UTF-8. Save it as UTF-8 and try again.")
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
        awaitText(R.id.txt_filename, "TXT: 발음.txt")
        pick(R.id.import_txt, "text/plain", "large.txt")
        awaitText(R.id.input_error, "This TXT is too large. Choose a file no larger than 64 KiB.")
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
    }

    @Test fun invalidPdfPreservesPreviousSelection() {
        pick(R.id.select_pdf, "application/pdf", "합창.pdf")
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
        pick(R.id.select_pdf, "application/pdf", "invalid.pdf")
        awaitText(R.id.input_error, "Could not read this PDF. Choose an accessible PDF score.")
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
    }

    @Test fun selectedPdfOpensViewerAndReturningPreservesPronunciation() {
        pick(R.id.select_pdf, "application/pdf", "합창.pdf")
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
        scenario.onActivity { it.findViewById<EditText>(R.id.pronunciation).setText(FixtureProvider.KOREAN) }
        onView(withId(R.id.view_pdf)).perform(scrollTo(), click())
        onView(withId(R.id.viewer_filename)).check(matches(withText("합창.pdf")))
        onView(withId(R.id.close_viewer)).perform(click())
        awaitText(R.id.pronunciation, FixtureProvider.KOREAN)
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
    }

    @Test fun persistedPdfReopensInNewSessionAndRevokedPermissionShowsRecovery() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.contentResolver.call(FixtureProvider.uri("합창.pdf"), "grant", "합창.pdf", null)
        pick(R.id.select_pdf, "application/pdf", "합창.pdf")
        awaitText(R.id.pdf_access_status, "This PDF can reopen next time while the file remains available.")
        scenario.close()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        awaitReady()
        awaitText(R.id.pdf_filename, "PDF: 합창.pdf")
        scenario.close()
        context.contentResolver.call(FixtureProvider.uri("합창.pdf"), "revoke", "합창.pdf", null)
        scenario = ActivityScenario.launch(MainActivity::class.java)
        awaitReady()
        awaitText(R.id.pdf_filename, "No PDF selected")
        awaitText(R.id.input_error, "The last PDF is no longer accessible. Select the score again.")
    }

    private fun awaitReady() {
        val deadline = System.nanoTime() + 5_000_000_000L
        var ready = false
        do {
            scenario.onActivity { ready = it.findViewById<Button>(R.id.select_pdf).isEnabled }
            if (ready) return
            Thread.sleep(25)
        } while (System.nanoTime() < deadline)
        org.junit.Assert.assertTrue("Input screen ready", ready)
    }

    private fun pick(button: Int, mime: String, name: String) {
        // Reset recorded intents and stubs between picks while retaining the same Activity.
        Intents.release()
        Intents.init()
        intending(hasAction(Intent.ACTION_OPEN_DOCUMENT)).respondWith(
            ActivityResult(Activity.RESULT_OK, Intent().setData(FixtureProvider.uri(name))),
        )
        onView(withId(button)).perform(scrollTo(), click())
        intended(allOf(hasAction(Intent.ACTION_OPEN_DOCUMENT), hasType("*/*"), hasExtra(Intent.EXTRA_MIME_TYPES, arrayContaining(mime)), hasExtra(Intent.EXTRA_LOCAL_ONLY, true)))
    }

    private fun awaitText(id: Int, expected: String) {
        val deadline = System.nanoTime() + 5_000_000_000L
        var actual = ""
        do {
            scenario.onActivity { actual = it.findViewById<TextView>(id).text.toString() }
            if (actual == expected) return
            Thread.sleep(25)
        } while (System.nanoTime() < deadline)
        assertEquals(expected, actual)
    }
}
