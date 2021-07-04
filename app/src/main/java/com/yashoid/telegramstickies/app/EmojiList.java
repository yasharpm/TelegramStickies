package com.yashoid.telegramstickies.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

public class EmojiList {

    private static final String TAG = "EmojiList";

    private static final String ORDERING = "emoji-ordering.txt";
    private static final String DIR = "emojis";
    private static final int EMOJI_SIZE = 160;

    private static final HashMap<String, String> COLOR_MAPPING = new HashMap<>(5);
    private static final HashMap<String, String> GENDER_MAPPING = new HashMap<>(2);

    static {
        COLOR_MAPPING.put("_u1F3FB", "1");
        COLOR_MAPPING.put("_u1F3FC", "2");
        COLOR_MAPPING.put("_u1F3FD", "3");
        COLOR_MAPPING.put("_u1F3FE", "4");
        COLOR_MAPPING.put("_u1F3FF", "5");

        GENDER_MAPPING.put("_u2640", "W");
        GENDER_MAPPING.put("_u2642", "M");
    }

    public interface EmojiListCallback {

        void onEmojiListReady(EmojiList emojiList);

    }

    private static EmojiList mInstance = null;

    private static boolean sLoading = false;
    private static final List<EmojiListCallback> sCallbacks = new LinkedList<>();

    synchronized public static void get(Context context, EmojiListCallback callback) {
        if (mInstance != null) {
            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> callback.onEmojiListReady(mInstance), 0);
            return;
        }

        sCallbacks.add(callback);

        if (sLoading) {
            return;
        }

        sLoading = true;

        DefaultTaskManager.getInstance().runTask(TaskManager.CALCULATION, () -> {
            EmojiList emojiList = new EmojiList(context.getApplicationContext());

            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> {
                synchronized (EmojiList.class) {
                    sLoading = false;

                    mInstance = emojiList;

                    ArrayList<EmojiListCallback> callbacks = new ArrayList<>(sCallbacks);

                    sCallbacks.clear();

                    for (EmojiListCallback c: callbacks) {
                        c.onEmojiListReady(mInstance);
                    }
                }
            }, 0);
        }, 0);
    }

    public static String getCharacters(String emoji) {
        emoji = emoji.substring(0, emoji.indexOf("."));

        String[] parts = emoji.split("_");

        StringBuilder sb = new StringBuilder();

        for (String part: parts) {
            if (part.length() == 5) {
                sb.append("\\u").append(part.substring(1));
            }
            else {
                // https://datacadamia.com/data/type/text/surrogate
                // view-source:https://datacadamia.com/data/type/text/surrogate L781

                int s = Integer.parseInt(part.substring(1), 16);
                int h = (s - 0x10000) / 0x400 + 0xD800;
                int l = ((s - 0x10000) % 0x400) + 0xDC00;

                sb
                        .append((char) h)
                        .append((char) l);
            }
        }

        return sb.toString();
    }

    private final AssetManager mAssets;

    private List<String> mEmojis;

    private final Map<String, Bitmap> mBitmaps = new HashMap<>();
    private final Map<String, List<WeakReference<Object>>> mReferences = new HashMap<>();
    private final Map<String, List<BitmapLoader.BitmapCallback>> mCallbacks = new HashMap<>();

    private EmojiList(Context context) {
        mAssets = context.getAssets();

        try {
            String[] availableEmojis = mAssets.list(DIR);
            Arrays.sort(availableEmojis);

            mEmojis = new ArrayList<>(2980);

            Scanner scanner = new Scanner(mAssets.open(ORDERING));
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();
            scanner.nextLine();

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("U")) {
                    line = line.substring(0, line.indexOf(";")).trim();
                    line = line.replaceAll("\\+", "").replaceAll(" ", "_").replaceAll("U", "u");

                    if (Arrays.binarySearch(availableEmojis, line + ".png") >= 0) {
                        mEmojis.add(line + ".png");
                        continue;
                    }

                    if (line.contains("_u200D")) {
                        line = line.replaceAll("_u200D", "");

                        if (Arrays.binarySearch(availableEmojis, line + ".png") >= 0) {
                            mEmojis.add(line + ".png");
                            continue;
                        }

                        for (String key: COLOR_MAPPING.keySet()) {
                            if (line.contains(key)) {
                                line = line.replaceAll(key, "") + "." + COLOR_MAPPING.get(key);
                                break;
                            }
                        }

                        if (Arrays.binarySearch(availableEmojis, line + ".png") >= 0) {
                            mEmojis.add(line + ".png");
                            continue;
                        }

                        for (String key: GENDER_MAPPING.keySet()) {
                            if (line.contains(key)) {
                                line = line.replaceAll(key, "") + "." + GENDER_MAPPING.get(key);
                                break;
                            }
                        }

                        if (Arrays.binarySearch(availableEmojis, line + ".png") >= 0) {
                            mEmojis.add(line + ".png");
                            continue;
                        }
                    }

                    if (Arrays.binarySearch(availableEmojis, line + ".0.png") >= 0) {
                        mEmojis.add(line + ".0.png");
                        continue;
                    }
                }
            }

            scanner.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to get list of emojis.", e);

            mEmojis = new ArrayList<>(0);
        }
    }

    public int getCount() {
        return mEmojis.size();
    }

    public String getEmojiAt(int index) {
        return mEmojis.get(index);
    }

    public Drawable getEmojiDrawable(int index) {
        return new EmojiDrawable(mEmojis.get(index));
    }

    public Drawable getEmojiDrawable(String emoji) {
        return new EmojiDrawable(emoji);
    }

    synchronized private void getBitmap(String emoji, Object handle, BitmapLoader.BitmapCallback callback) {
        getReferences(emoji, true).add(new WeakReference<>(handle));

        if (mBitmaps.containsKey(emoji)) {
            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> callback.onBitmapReady(mBitmaps.get(emoji)), 0);
            return;
        }

        if (mCallbacks.containsKey(emoji)) {
            mCallbacks.get(emoji).add(callback);
            return;
        }

        List<BitmapLoader.BitmapCallback> callbacks = new ArrayList<>();
        callbacks.add(callback);
        mCallbacks.put(emoji, callbacks);

        BitmapLoader.loadBitmap(() -> mAssets.open(DIR + "/" + emoji), EMOJI_SIZE, bitmap -> {
            synchronized (this) {
                if (getReferences(emoji, false) == null) {
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    mCallbacks.remove(emoji);
                    return;
                }

                mBitmaps.put(emoji, bitmap);

                List<BitmapLoader.BitmapCallback> cList = new ArrayList<>(mCallbacks.get(emoji));

                mCallbacks.remove(emoji);

                for (BitmapLoader.BitmapCallback c: cList) {
                    c.onBitmapReady(bitmap);
                }
            }
        });
    }

    private List<WeakReference<Object>> getReferences(String key, boolean create) {
        List<WeakReference<Object>> references = mReferences.get(key);

        if (references != null) {
            ListIterator<WeakReference<Object>> iterator = references.listIterator();

            while (iterator.hasNext()) {
                WeakReference<Object> reference = iterator.next();

                if (reference.get() == null) {
                    iterator.remove();
                }
            }
        }

        if ((references == null || references.isEmpty()) && !create) {
            mReferences.remove(key);
            return null;
        }

        if (references == null) {
            references = new ArrayList<>();

            mReferences.put(key, references);
        }

        return references;
    }

    private class EmojiDrawable extends Drawable implements BitmapLoader.BitmapCallback {

        private final String mEmoji;

        private Bitmap mBitmap = null;

        public EmojiDrawable(String emoji) {
            mEmoji = emoji;

            getBitmap(mEmoji, this, this);
        }

        @Override
        public void onBitmapReady(Bitmap bitmap) {
            mBitmap = bitmap;

            invalidateSelf();
        }

        @Override
        public int getIntrinsicWidth() {
            return EMOJI_SIZE;
        }

        @Override
        public int getIntrinsicHeight() {
            return EMOJI_SIZE;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (mBitmap == null) {
                return;
            }

            canvas.drawBitmap(mBitmap, null, getBounds(), null);
        }

        @Override public void setAlpha(int alpha) { }
        @Override public void setColorFilter(@Nullable ColorFilter colorFilter) { }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

    }

}
