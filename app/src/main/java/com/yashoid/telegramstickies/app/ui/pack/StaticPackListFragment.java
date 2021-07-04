package com.yashoid.telegramstickies.app.ui.pack;

import androidx.activity.result.ActivityResultLauncher;

import com.yashoid.mmv.ModelFeatures;
import com.yashoid.mmv.SingleShotTarget;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.model.stickerpack.StaticStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StickerPack;
import com.yashoid.telegramstickies.app.model.stickerpacklist.StaticStickerPackList;
import com.yashoid.telegramstickies.app.ui.staticstickereditor.StickerEditorActivity;

public class StaticPackListFragment extends PackListFragment {

    private final ActivityResultLauncher<String[]> mNewPackLauncher;

    private String mPendingPackId;

    public StaticPackListFragment() {
        super(
                R.color.static_theme,
                R.drawable.img_onboarding_static,
                R.string.packlist_create_static,
                R.string.packinformation_title_static,
                StaticStickerPack.TYPE_STATIC_STICKER_PACK,
                StaticPackListAdapter.class
        );

        mNewPackLauncher = registerForActivityResult(StickerEditorActivity.getLaunchContract(), stickerId -> {
            if (stickerId == null) {
                return;
            }

            ModelFeatures packFeatures = new ModelFeatures.Builder()
                    .add(StaticStickerPack.TYPE, StaticStickerPack.TYPE_STATIC_STICKER_PACK)
                    .add(StaticStickerPack.ID, mPendingPackId)
                    .build();

            SingleShotTarget.get(packFeatures, pack -> {
                ModelFeatures stickerFeatures = new ModelFeatures.Builder()
                        .add(Sticker.TYPE, Sticker.TYPE_STICKER)
                        .add(Sticker.ID, stickerId)
                        .build();

                pack.perform(StickerPack.ADD, stickerFeatures);
                pack.cache(true);

                SingleShotTarget.get(StaticStickerPackList.MODEL_FEATURES, packList -> {
                    packList.perform(StaticStickerPackList.ADD, packFeatures, 0);
                    packList.cache(true);
                });
            });

            startActivity(StickerPackActivity.getIntent(getContext(), StaticStickerPack.TYPE_STATIC_STICKER_PACK, mPendingPackId));
        });
    }

    @Override
    protected void onNewPackCreated(String packId) {
        mPendingPackId = packId;
        mNewPackLauncher.launch(new String[] { mPendingPackId, null});
    }

}
