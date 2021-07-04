package com.yashoid.telegramstickies.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.UUID;

public class Utils {

    private static final String TAG = "Utils";

    public interface SaveBitmapCallback {

        void onBitmapSaved(Uri uri);

    }

    public static ShapeDrawable createOvalShape(int color) {
        OvalShape shape = new OvalShape();
        ShapeDrawable drawable = new ShapeDrawable(shape);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return drawable;
    }

    public static Drawable getThemedDrawable(Context context, int drawableResId, int colorResId) {
        Drawable drawable = AppCompatResources.getDrawable(context, drawableResId);

        if (drawable != null) {
            drawable = drawable.mutate();
            drawable.setColorFilter(ContextCompat.getColor(context, colorResId), PorterDuff.Mode.SRC_IN);
        }

        return drawable;
    }

    public static File someFile(Context context, String extension) {
        return new File(context.getExternalCacheDir(), UUID.randomUUID().toString() + "." + extension);
    }

    public static void saveBitmapToFile(Bitmap bitmap, File file, Bitmap.CompressFormat format, SaveBitmapCallback callback) {
        DefaultTaskManager.getInstance().runTask(TaskManager.CALCULATION, () -> {
            Uri uri = null;

            try {
                saveBitmapToFile(bitmap, file, format);

                uri = Uri.fromFile(file);
            } catch (IOException e) {
                Log.e(TAG, "Failed to save bitmap to file.", e);
            }

            Uri result = uri;

            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> {
                callback.onBitmapSaved(result);
            }, 0);
        }, 0);
    }

    public static void saveBitmapToFile(Bitmap bitmap, File file, Bitmap.CompressFormat format) throws IOException {
        FileOutputStream output = new FileOutputStream(file);
        bitmap.compress(format, 80, output);
        output.close();
    }

    public static String streamToString(BitmapLoader.StreamOpener streamOpener) throws IOException {
        StringBuilder sb = new StringBuilder();

        Scanner scanner = new Scanner(streamOpener.openStream());

        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        scanner.close();

        return sb.toString();
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[512];
        int size = 0;

        while (size != -1) {
            size = input.read(buffer);

            if (size > 0) {
                output.write(buffer, 0, size);
            }
        }
    }

}
