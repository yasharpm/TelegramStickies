package com.yashoid.telegramstickies.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.yashoid.mmv.Managers;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.Utils;
import com.yashoid.telegramstickies.app.model.stickerpack.StickerPack;

public class PackInformationActivity extends TargetActivity {

    private static final String EXTRA_TITLE_RES_ID = "title_res_id";
    private static final String EXTRA_THEME_COLOR_ID = "theme_color_id";
    private static final String EXTRA_STICKER_PACK_TYPE = "sticker_pack_type";
    public static final String EXTRA_PACK_ID = "pack_id";

    public static Intent getIntent(Context context, int titleResId, int themeColorId, String stickerPackType, String packId) {
        Intent intent = getIntent(context, titleResId, themeColorId, stickerPackType);

        intent.putExtra(EXTRA_PACK_ID, packId);

        return intent;
    }

    public static Intent getIntent(Context context, int titleResId, int themeColorId, String stickerPackType) {
        Intent intent = new Intent(context, PackInformationActivity.class);

        intent.putExtra(EXTRA_TITLE_RES_ID, titleResId);
        intent.putExtra(EXTRA_THEME_COLOR_ID, themeColorId);
        intent.putExtra(EXTRA_STICKER_PACK_TYPE, stickerPackType);

        return intent;
    }

    private EditText mEditPackName;
    private EditText mEditCreatorName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packinformation);

        Intent intent = getIntent();

        mEditPackName = findViewById(R.id.edit_packname);
        mEditCreatorName = findViewById(R.id.edit_creatorname);

        findViewById(R.id.button_back).setOnClickListener(this::back);
        ((TextView) findViewById(R.id.text_title)).setText(intent.getIntExtra(EXTRA_TITLE_RES_ID, 0));

        View buttonNext = findViewById(R.id.button_next);

        buttonNext.setBackground(Utils.getThemedDrawable(this, R.drawable.large_button_background, intent.getIntExtra(EXTRA_THEME_COLOR_ID, 0)));
        buttonNext.setOnClickListener(this::next);

        final String stickerPackType = intent.getStringExtra(EXTRA_STICKER_PACK_TYPE);
        ModelFeatures modelFeatures;

        if (intent.hasExtra(EXTRA_PACK_ID)) {
            modelFeatures = new ModelFeatures.Builder()
                    .add(StickerPack.TYPE, stickerPackType)
                    .add(StickerPack.ID, intent.getStringExtra(EXTRA_PACK_ID))
                    .build();
        }
        else {
            modelFeatures = new ModelFeatures.Builder()
                    .add(StickerPack.TYPE, stickerPackType)
                    .build();
        }

        Managers.registerTarget(this, modelFeatures);
    }

    @Override
    protected void onModelChanged(Model model) {
        mEditPackName.setText(model.get(StickerPack.NAME));
        mEditCreatorName.setText(model.get(StickerPack.CREATOR));
    }

    private void back(View v) {
        finish();
    }

    private void next(View v) {
        String packName = mEditPackName.getText().toString().trim();
        String creatorName = mEditCreatorName.getText().toString().trim();

        if (packName.isEmpty()) {
            Toast.makeText(this, R.string.packinformation_no_pack_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (creatorName.isEmpty()) {
            Toast.makeText(this, R.string.packinformation_no_creator_name, Toast.LENGTH_SHORT).show();
            return;
        }

        Model model = getModel();

        model.set(StickerPack.NAME, packName);
        model.set(StickerPack.CREATOR, creatorName);

        Intent data = new Intent();
        data.putExtra(EXTRA_PACK_ID, (String) model.get(StickerPack.ID));

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected int getContentLayoutId() {
        return 0;
    }

}
