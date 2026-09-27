package com.choirscorepractice;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
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
        String name = uri.getLastPathSegment();
        if ("합창.pdf".equals(name) || "many.pdf".equals(name)) {
            return openScore(name, "many.pdf".equals(name) ? 24 : 3);
        }
        byte[] bytes;
        switch (uri.getLastPathSegment()) {
            case "발음.txt": bytes = KOREAN.getBytes(StandardCharsets.UTF_8); break;
            case "invalid.txt": bytes = new byte[] { (byte) 0xC3, 0x28 }; break;
            case "large.txt": bytes = new byte[65537]; Arrays.fill(bytes, (byte) 65); break;
            case "invalid.pdf": bytes = "not a PDF".getBytes(StandardCharsets.UTF_8); break;
            default: throw new FileNotFoundException(uri.getLastPathSegment());
        }
        File file = new File(getContext().getCacheDir(), "input-fixture-" + name.hashCode());
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(bytes);
        } catch (IOException exception) {
            throw new FileNotFoundException(exception.getMessage());
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }
    private ParcelFileDescriptor openScore(String name, int pages) throws FileNotFoundException {
        File file = new File(getContext().getCacheDir(), "score-fixture-" + name.hashCode() + ".pdf");
        if (!file.exists()) {
            PdfDocument document = new PdfDocument();
            try (FileOutputStream output = new FileOutputStream(file)) {
                Paint paint = new Paint();
                for (int index = 0; index < pages; index++) {
                    int width = index % 2 == 0 ? 600 : 800;
                    int height = index % 2 == 0 ? 800 : 600;
                    PdfDocument.Page page = document.startPage(new PdfDocument.PageInfo.Builder(width, height, index + 1).create());
                    paint.setColor(index % 2 == 0 ? Color.BLUE : Color.RED);
                    page.getCanvas().drawRect(20, 20, 80, 80, paint);
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(28);
                    page.getCanvas().drawText("Synthetic choir score - page " + (index + 1), 30, 130, paint);
                    paint.setStrokeWidth(1);
                    for (int staff = 0; staff < 3; staff++) {
                        for (int line = 0; line < 5; line++) {
                            int y = 200 + staff * 100 + line * 10;
                            page.getCanvas().drawLine(30, y, width - 30, y, paint);
                        }
                    }
                    document.finishPage(page);
                }
                document.writeTo(output);
            } catch (IOException exception) {
                throw new FileNotFoundException(exception.getMessage());
            } finally {
                document.close();
            }
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override public Bundle call(String method, String arg, Bundle extras) {
        if ("grant".equals(method)) {
            getContext().grantUriPermission("com.choirscorepractice", uri(arg),
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        } else if ("revoke".equals(method)) {
            getContext().revokeUriPermission(uri(arg), Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            throw new IllegalArgumentException(method);
        }
        return Bundle.EMPTY;
    }

    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
        throw new UnsupportedOperationException();
    }
}
