package com.yashoid.telegramstickies.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;

import java.io.IOException;
import java.io.InputStream;

public class BitmapLoader {

    private static final String TAG = "BitmapLoader";

    public interface BitmapCallback {

        void onBitmapReady(Bitmap bitmap);

    }

    public interface StreamOpener {

        InputStream openStream() throws IOException;

    }

    public static void loadBitmap(Context context, Uri uri, int size, BitmapCallback callback) {
        loadBitmap(() -> openInputStream(context, uri), size, callback);
    }

    public static void loadBitmap(StreamOpener streamOpener, int size, BitmapCallback callback) {
        DefaultTaskManager.getInstance().runTask(TaskManager.CALCULATION, () -> {
            try {
                Bitmap bitmap = loadBitmap(streamOpener, size);

                if (bitmap != null) {
                    DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> callback.onBitmapReady(bitmap), 0);
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to load bitmap", e);
            }
        }, 0);
    }

    public static Bitmap loadBitmap(Context context, Uri uri, int size) throws IOException {
        return loadBitmap(() -> openInputStream(context, uri), size);
    }

    public static Bitmap loadBitmap(StreamOpener streamOpener, int size) throws IOException {
        InputStream input = streamOpener.openStream();

        if (input == null) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(input, null, options);

        input.close();

        int bitmapSize = Math.min(options.outWidth, options.outHeight);

        while (bitmapSize > size * 3 / 2) {
            options.inSampleSize *= 2;
            bitmapSize /= 2;
        }

        options.inJustDecodeBounds = false;

        input = streamOpener.openStream();

        Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);

        input.close();

        return bitmap;
    }

    private static InputStream openInputStream(Context context, Uri uri) throws IOException {
        return context.getContentResolver().openInputStream(uri);
    }

}
