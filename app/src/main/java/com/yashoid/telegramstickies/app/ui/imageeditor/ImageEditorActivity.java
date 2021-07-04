package com.yashoid.telegramstickies.app.ui.imageeditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yashoid.telegramstickies.app.Crop;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Unavailable;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.ui.BaseActivity;
import com.yashoid.telegramstickies.app.ui.widget.ChessBoardDrawable;

import java.io.File;
import java.util.UUID;

public class ImageEditorActivity extends BaseActivity implements CropPicker.OnCropChoiceCallback, EraserPicker.OnEraserSelectedCallback {

    public static Intent getIntent(Context context, Uri uri) {
        Intent intent = new Intent(context, ImageEditorActivity.class);

        intent.setData(uri);

        return intent;
    }

    public static ActivityResultContract<Uri, Uri> getLaunchContract() {
        return new ActivityResultContract<Uri, Uri>() {

            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Uri input) {
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
    private ImageEditor mEditor;

    private CropView mCropView = null;

    private int mEraserSize = -1;
    private float mEraserOpacity = -1;

    private boolean mSaving = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageeditor);

        mButtonBack = findViewById(R.id.button_back);
        mTextTitle = findViewById(R.id.text_title);
        mButtonSave = findViewById(R.id.button_save);
        mButtonCancel = findViewById(R.id.button_cancel);
        mButtonDone = findViewById(R.id.button_done);
        mEditor = findViewById(R.id.editor);

        findViewById(R.id.button_crop).setOnClickListener(v -> new CropPicker(this).show(getSupportFragmentManager(), getContentLayoutId()));
        findViewById(R.id.button_erase).setOnClickListener(v -> new EraserPicker(mEraserSize, mEraserOpacity, this).show(getSupportFragmentManager(), getContentLayoutId()));

        mButtonBack.setOnClickListener(v -> finish());
        mButtonSave.setOnClickListener(this::save);
        mButtonCancel.setOnClickListener(this::onEditCancelled);
        mButtonDone.setOnClickListener(this::onEditDone);

        setEditMode(false);

        Uri imageUri = getIntent().getData();
        mEditor.setImageUri(imageUri);
        mEditor.setBackground(new ChessBoardDrawable(this));

        Unavailable.mark(findViewById(R.id.button_corners));
    }

    @Override
    public void onCropChoice(int choice) {
        switch (choice) {
            case Crop.CROP_CIRCULAR:
            case Crop.CROP_RECT:
                mCropView = new CropView(this);
                mCropView.setCropMode(choice);
                mEditor.addView(mCropView);

                setEditMode(true);
                return;
        }
    }

    @Override
    public void onEraserSelected(int size, float opacity) {
        mEraserSize = size;
        mEraserOpacity = opacity;

        setEditMode(true);

        mEditor.startErasing(mEraserSize / 2f, mEraserOpacity);
    }

    public void onEditDone(View v) {
        setEditMode(false);

        if (mCropView != null) {
            RectF cropRatios = new RectF();

            int cropMode = mCropView.getCropMode();
            mCropView.getCropRatios(cropRatios);

            mEditor.removeView(mCropView);

            mEditor.crop(cropRatios, cropMode);

            mCropView = null;
        }

        if (mEditor.isErasing()) {
            mEditor.applyErasing();
        }
    }

    public void onEditCancelled(View v) {
        setEditMode(false);

        if (mCropView != null) {
            mEditor.removeView(mCropView);
            mCropView = null;
        }

        if (mEditor.isErasing()) {
            mEditor.cancelErasing();
        }
    }

    private void setEditMode(boolean enabled) {
        mButtonBack.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mTextTitle.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mButtonSave.setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        mButtonCancel.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
        mButtonDone.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);

        findViewById(R.id.button_crop).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.button_erase).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.button_corners).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
        findViewById(R.id.toolbar).setVisibility(enabled ? View.INVISIBLE : View.VISIBLE);
    }

    private void save(View v) {
        if (mSaving) {
            return;
        }

        mSaving = true;

        Bitmap bitmap = mEditor.getBitmap();

        if (bitmap == null) {
            mSaving = false;
            return;
        }

        File file = new File(getExternalCacheDir(), UUID.randomUUID().toString());

        Utils.saveBitmapToFile(bitmap, file, Bitmap.CompressFormat.PNG, uri -> {
            if (uri != null) {
                Intent data = new Intent();
                data.setData(uri);
                setResult(RESULT_OK, data);
            }
            finish();
        });
    }

    @Override
    protected int getContentLayoutId() {
        return R.id.overlay;
    }

}
