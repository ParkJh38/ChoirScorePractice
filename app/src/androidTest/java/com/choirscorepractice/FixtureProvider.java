package com.choirscorepractice;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Test APK runs its provider separately, so this fixture needs no target-app Kotlin runtime. */
public class FixtureProvider extends ContentProvider {
    public static final String KOREAN = "\uFEFF  한글 한글\r\n발음 🎵\n\t ";

    public static Uri uri(String name) {
        return new Uri.Builder().scheme("content")
            .authority("com.choirscorepractice.test.documents").appendPath(name).build();
    }

    @Override public boolean onCreate() { return true; }
    @Override public String getType(Uri uri) {
        return uri.getLastPathSegment().endsWith(".pdf") ? "application/pdf" : "text/plain";
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[] { OpenableColumns.DISPLAY_NAME });
        cursor.addRow(new Object[] { uri.getLastPathSegment() });
        return cursor;
    }
    @Override public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (!"r".equals(mode)) throw new FileNotFoundException("Read only");
        byte[] bytes;
        switch (uri.getLastPathSegment()) {
            case "합창.pdf":
                bytes = "%PDF-1.4\nSynthetic header fixture; no recognition or rendering".getBytes(StandardCharsets.UTF_8);
                break;
            case "발음.txt": bytes = KOREAN.getBytes(StandardCharsets.UTF_8); break;
            case "invalid.txt": bytes = new byte[] { (byte) 0xC3, 0x28 }; break;
            case "large.txt": bytes = new byte[65537]; Arrays.fill(bytes, (byte) 65); break;
            case "invalid.pdf": bytes = "not a PDF".getBytes(StandardCharsets.UTF_8); break;
            default: throw new FileNotFoundException(uri.getLastPathSegment());
        }
        File file = new File(getContext().getCacheDir(), "input-fixture");
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
        } catch (IOException exception) {
            throw new FileNotFoundException(exception.getMessage());
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new UnsupportedOperationException();
    }
}
