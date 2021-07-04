package com.yashoid.telegramstickies.app.ui.animatedstickereditor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.yashoid.telegramstickies.StickerColor;
import com.yashoid.telegramstickies.StickyThingy;
import com.yashoid.telegramstickies.app.AnimationList;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.ui.BaseActivity;
import com.yashoid.telegramstickies.app.ui.ColorPicker;
import com.yashoid.telegramstickies.app.ui.widget.ChessBoardDrawable;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AnimationEditorActivity extends BaseActivity implements AnimationColorSelector.OnAnimationColorSelectedListener, ColorPicker.OnColorSelectedListener {

    private static final String TAG = "AnimationEditor";

    private static final String EXTRA_ANIMATION = "animation";

    public static Intent getIntent(Context context, String animation) {
        Intent intent = new Intent(context, AnimationEditorActivity.class);

        intent.putExtra(EXTRA_ANIMATION, animation);

        return intent;
    }

    public static ActivityResultContract<String, Uri> getLaunchContract() {
        return new ActivityResultContract<String, Uri>() {

            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, String input) {
                return getIntent(context, input);
            }

            @Override
            public Uri parseResult(int resultCode, @Nullable Intent intent) {
                return resultCode == RESULT_OK ? intent.getData() : null;
            }

        };
    }

    private View mButtonBack;
    private View mTextTitle;
    private View mButtonSave;
    private View mButtonCancel;
    private View mButtonDone;
    private LottieAnimationView mEditor;

    private StickyThingy mStickyThingy = null;

    private StickerColor mSourceColor = null;

    private boolean mSaving = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animationeditor);

        mButtonBack = findViewById(R.id.button_back);
        mTextTitle = findViewById(R.id.text_title);
        mButtonSave = findViewById(R.id.button_save);
        mButtonCancel = findViewById(R.id.button_cancel);
        mButtonDone = findViewById(R.id.button_done);
        mEditor = findViewById(R.id.editor);

        findViewById(R.id.button_reset).setOnClickListener(this::reset);
        findViewById(R.id.button_color).setOnClickListener(v -> {
            try {
                new AnimationColorSelector(mStickyThingy.getColors(), this).show(getSupportFragmentManager(), getContentLayoutId());
            } catch (JSONException e) { }
        });

        mButtonBack.setOnClickListener(v -> finish());
        mButtonSave.setOnClickListener(this::save);
        mButtonCancel.setOnClickListener(this::onEditCancelled);
        mButtonDone.setOnClickListener(this::onEditDone);

        setEditMode(false);

        reset(null);

        mEditor.setRepeatMode(LottieDrawable.RESTART);
        mEditor.setRepeatCount(LottieDrawable.INFINITE);
        mEditor.setBackground(new ChessBoardDrawable(this));
    }

    private void setJSON(String json) {
        mEditor.setAnimationFromJson(json, null);
        mEditor.playAnimation();
    }

    private void reset(View v) {
        String animation = getIntent().getStringExtra(EXTRA_ANIMATION);

        AnimationList.get(this, animationList -> {
            try {
                String json = animationList.getAnimationJSON(animation);
                mStickyThingy = new StickyThingy(json);
                setJSON(json);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Failed to get animation JSON.", e);
                finish();
            }
        });
    }

    @Override
    public void onAnimationColorSelected(StickerColor color) {
        mSourceColor = color;

        new ColorPicker(this).show(getSupportFragmentManager(), getContentLayoutId());
    }

    @Override
    public void onColorSelected(int color) {
        mStickyThingy.replaceColor(mSourceColor, new StickerColor(color));

        try {
            setJSON(mStickyThingy.toJSON().toString());
        } catch (JSONException e) { }
    }

    public void onEditDone(View v) {
        setEditMode(false);
    }

    public void onEditCancelled(View v) {
        setEditMode(false);
    }

    private void setEditMode(boolean enabled) {
        mButtonBack.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mTextTitle.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mButtonSave.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mButtonCancel.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        mButtonDone.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);

        findViewById(R.id.button_reset).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.button_color).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
    }

    private void save(View v) {
        if (mSaving) {
            return;
        }

        mSaving = true;

        String json = null;

        try {
            json = mStickyThingy.toJSON().toString();
        } catch (JSONException e) { }

        if (json == null) {
            mSaving = false;
            return;
        }

        File file = new File(getExternalCacheDir(), UUID.randomUUID().toString());

        try {
            FileOutputStream output = new FileOutputStream(file);
            output.write(json.getBytes());
            output.close();
        } catch (IOException e) { }

        Intent data = new Intent();
        data.setData(Uri.fromFile(file));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected int getContentLayoutId() {
        return R.id.overlay;
    }

}
