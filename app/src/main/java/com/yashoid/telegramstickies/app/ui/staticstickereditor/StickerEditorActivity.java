package com.yashoid.telegramstickies.app.ui.staticstickereditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yashoid.mmv.Managers;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Unavailable;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.ui.TargetActivity;
import com.yashoid.telegramstickies.app.ui.imageeditor.ImageEditorActivity;
import com.yashoid.telegramstickies.app.ui.packeditor.EmojiPicker;
import com.yashoid.telegramstickies.app.ui.widget.ChessBoardDrawable;
import com.yashoid.telegramstickies.app.ui.widget.StickerEditor;

import java.io.File;
import java.util.List;

public class StickerEditorActivity extends TargetActivity implements EmojiPicker.OnEmojisSelectedListener {

    private static final String EXTRA_PACK_ID = "pack_id";
    private static final String EXTRA_STICKER_ID = "sticker_id";

    public static Intent getIntent(Context context, String packId, String stickerId) {
        Intent intent = new Intent(context, StickerEditorActivity.class);

        intent.putExtra(EXTRA_PACK_ID, packId);
        intent.putExtra(EXTRA_STICKER_ID, stickerId);

        return intent;
    }

    public static ActivityResultContract<String[], String> getLaunchContract() {
        return new ActivityResultContract<String[], String>() {

            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, String[] input) {
                return getIntent(context, input[0], input[1]);
            }

            @Override
            public String parseResult(int resultCode, @Nullable Intent intent) {
                return resultCode == RESULT_OK ? intent.getStringExtra(EXTRA_STICKER_ID) : null;
            }

        };
    }

    private ActivityResultLauncher<Uri> mImageEditorLauncher;

    private StickerEditor mEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickereditor);

        mEditor = findViewById(R.id.editor);
        mEditor.setBackground(new ChessBoardDrawable(this));

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_save).setOnClickListener(this::save);
        findViewById(R.id.button_photo).setOnClickListener(this::addPhoto);

        mImageEditorLauncher = registerForActivityResult(ImageEditorActivity.getLaunchContract(), uri -> {
            ImageView image = new ImageView(StickerEditorActivity.this);
            image.setImageURI(uri);
            mEditor.addView(image);
        });

        String stickerId = getIntent().getStringExtra(EXTRA_STICKER_ID);

        ModelFeatures stickerFeatures;

        if (stickerId == null) {
            stickerFeatures= new ModelFeatures.Builder()
                    .add(Sticker.TYPE, Sticker.TYPE_STICKER)
                    .build();
        }
        else {
            stickerFeatures= new ModelFeatures.Builder()
                    .add(Sticker.TYPE, Sticker.TYPE_STICKER)
                    .add(Sticker.ID, stickerId)
                    .build();
        }

        Managers.registerTarget(this, stickerFeatures);

        Unavailable.mark(findViewById(R.id.button_draw));
        Unavailable.mark(findViewById(R.id.button_text));
        Unavailable.mark(findViewById(R.id.button_sticker));
    }

    private void addPhoto(View v) {
        PhotoPicker photoPicker = new PhotoPicker();
        photoPicker.show(getSupportFragmentManager(), R.id.overlay);
        photoPicker.setOnPhotoSelectedListener(uri -> mImageEditorLauncher.launch(uri));
    }

    private void save(View v) {
        new EmojiPicker(this).show(getSupportFragmentManager(), getContentLayoutId());
    }

    @Override
    public void onEmojisSelected(List<String> selectedEmojis) {
        Bitmap bitmap = mEditor.getStickerBitmap();

        File file = Utils.someFile(this, "png");

        Utils.saveBitmapToFile(bitmap, file, Bitmap.CompressFormat.PNG, uri -> {
            Model model = getModel();

            model.set(Sticker.IMAGE_FILE, uri.toString());
            model.set(Sticker.EMOJIS, selectedEmojis);
            model.cache(true);

            Intent data = new Intent();
            data.putExtra(EXTRA_STICKER_ID, (String) model.get(Sticker.ID));
            setResult(RESULT_OK, data);
            finish();
        });
    }

    @Override
    protected void onModelChanged(Model model) {

    }

    @Override
    protected int getContentLayoutId() {
        return R.id.overlay;
    }

}
