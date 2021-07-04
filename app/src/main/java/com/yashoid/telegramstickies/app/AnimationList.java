package com.yashoid.telegramstickies.app;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AnimationList {

    private static final String TAG = "AnimationList";

    private static final String DIR = "animated";

    public interface AnimationListCallback {

        void onAnimationListReady(AnimationList animationList);

    }

    private static AnimationList mInstance = null;

    private static boolean sLoading = false;
    private static final List<AnimationListCallback> sCallbacks = new LinkedList<>();

    synchronized public static void get(Context context, AnimationListCallback callback) {
        if (mInstance != null) {
            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> callback.onAnimationListReady(mInstance), 0);
            return;
        }

        sCallbacks.add(callback);

        if (sLoading) {
            return;
        }

        sLoading = true;

        DefaultTaskManager.getInstance().runTask(TaskManager.CALCULATION, () -> {
            AnimationList animationList = new AnimationList(context.getApplicationContext());

            DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> {
                synchronized (AnimationList.class) {
                    sLoading = false;

                    mInstance = animationList;

                    ArrayList<AnimationListCallback> callbacks = new ArrayList<>(sCallbacks);

                    sCallbacks.clear();

                    for (AnimationListCallback c: callbacks) {
                        c.onAnimationListReady(mInstance);
                    }
                }
            }, 0);
        }, 0);
    }

    private final AssetManager mAssets;

    private List<String> mAnimations;

    private AnimationList(Context context) {
        mAssets = context.getAssets();

        try {
            String[] animationArray = mAssets.list(DIR);
            Arrays.sort(animationArray);

            mAnimations = new ArrayList<>(animationArray.length);
            mAnimations.addAll(Arrays.asList(animationArray));
        } catch (IOException e) {
            Log.e(TAG, "Failed to get list of emojis.", e);

            mAnimations = new ArrayList<>(0);
        }
    }

    public int getCount() {
        return mAnimations.size();
    }

    public String getAnimationAt(int index) {
        return mAnimations.get(index);
    }

    public String getAnimationJSON(int index) throws IOException {
        return getAnimationJSON(mAnimations.get(index));
    }

    public String getAnimationJSON(String animation) throws IOException {
        return Utils.streamToString(() -> mAssets.open(DIR + "/" + animation));
    }

}
