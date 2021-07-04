package com.yashoid.telegramstickies.app.ui.animatedstickereditor;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.yashoid.mmv.Managers;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.StickyThingy;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Rules;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.ui.TargetActivity;
import com.yashoid.telegramstickies.app.ui.imageeditor.ImageEditorActivity;
import com.yashoid.telegramstickies.app.ui.packeditor.EmojiPicker;
import com.yashoid.telegramstickies.app.ui.widget.AnimationView;
import com.yashoid.telegramstickies.app.ui.widget.ChessBoardDrawable;
import com.yashoid.telegramstickies.app.ui.widget.StickerEditor;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class AnimatedStickerEditorActivity extends TargetActivity implements EmojiPicker.OnEmojisSelectedListener {

    private static final String TAG = "AnimatedStickerEditor";

    private static final String EXTRA_PACK_ID = "pack_id";
    private static final String EXTRA_STICKER_ID = "sticker_id";

    public static Intent getIntent(Context context, String packId, String stickerId) {
        Intent intent = new Intent(context, AnimatedStickerEditorActivity.class);

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

    private ActivityResultLauncher<String> mAnimationEditorLauncher;

    private StickerEditor mEditor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animatedstickereditor);

        mEditor = findViewById(R.id.editor);
        mEditor.setBackground(new ChessBoardDrawable(this));

        findViewById(R.id.button_back).setOnClickListener(v -> finish());
        findViewById(R.id.button_save).setOnClickListener(this::save);
        findViewById(R.id.button_add).setOnClickListener(v -> new AnimationPicker(animation -> mAnimationEditorLauncher.launch(animation)).show(getSupportFragmentManager(), getContentLayoutId()));

        mAnimationEditorLauncher = registerForActivityResult(AnimationEditorActivity.getLaunchContract(), uri -> {
            if (uri == null) {
                return;
            }

            AnimationView image = new AnimationView(AnimatedStickerEditorActivity.this);
            image.setTag(uri);

            try {
                String json = Utils.streamToString(() -> getContentResolver().openInputStream(uri));

                StickyThingy tempThingy = new StickyThingy(json);
                image.setIntrinsicSize(tempThingy.getWidth(), tempThingy.getHeight());

                image.setAnimationFromJson(json, uri.toString());
                image.setRepeatMode(LottieDrawable.RESTART);
                image.setRepeatCount(LottieDrawable.INFINITE);
                image.playAnimation();

                mEditor.addView(image);
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Failed to get json for animation.", e);
            }
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
    }

    private void save(View v) {
        new EmojiPicker(this).show(getSupportFragmentManager(), getContentLayoutId());
    }

    @Override
    public void onEmojisSelected(List<String> selectedEmojis) {
        try {
            StickyThingy stickyThingy = new StickyThingy();

            for (int i = mEditor.getChildCount() - 1; i >= 0; i--) {
                View child = mEditor.getChildAt(i);
                StickerEditor.LayoutParams params = (StickerEditor.LayoutParams) child.getLayoutParams();

                Uri uri = (Uri) child.getTag();

                if (uri == null) {
                    continue;
                }

                String json = Utils.streamToString(() -> getContentResolver().openInputStream(uri));

                if (json == null) {
                    continue;
                }

                StickyThingy childThingy = new StickyThingy(json);

                float adjustScale = childThingy.getWidth() / params.width;

                childThingy.rotateBy(params.rotation);
                childThingy.resize(0.5f, 0.5f, childThingy.getWidth() * params.scale, childThingy.getHeight() * params.scale);
                childThingy.translate(params.left * adjustScale, params.top * adjustScale);

                stickyThingy.add(childThingy);
            }

            float width = stickyThingy.getWidth();
            float height = stickyThingy.getHeight();

            float newWidth;
            float newHeight;

            if (width > height) {
                newWidth = Rules.STICKER_SIZE;
                newHeight = height / width * newWidth;
            }
            else {
                newHeight = Rules.STICKER_SIZE;
                newWidth = width / height * newHeight;
            }

            stickyThingy.resize(0, 0, newWidth, newHeight);

            String json = stickyThingy.toJSON().toString();

            File file = Utils.someFile(this, "tgs");

            FileOutputStream output = new FileOutputStream(file);
            output.write(json.getBytes());
            output.close();

            Model model = getModel();

            model.set(Sticker.IMAGE_FILE, Uri.fromFile(file).toString());
            model.set(Sticker.EMOJIS, selectedEmojis);
            model.cache(true);

            Intent data = new Intent();
            data.putExtra(EXTRA_STICKER_ID, (String) model.get(Sticker.ID));
            setResult(RESULT_OK, data);
            finish();

        } catch (Throwable t) {
            Log.e(TAG, "Failed to save animated emoji.", t);
        }
    }

    @Override
    protected void onModelChanged(Model model) {

    }

    @Override
    protected int getContentLayoutId() {
        return R.id.overlay;
    }

}
