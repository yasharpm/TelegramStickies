package com.yashoid.telegramstickies.app.ui.pack;

import androidx.activity.result.ActivityResultLauncher;

import com.yashoid.mmv.ModelFeatures;
import com.yashoid.mmv.SingleShotTarget;
import com.yashoid.telegramstickies.app.R;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.model.stickerpack.AnimatedStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StaticStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StickerPack;
import com.yashoid.telegramstickies.app.model.stickerpacklist.AnimatedStickerPackList;
import com.yashoid.telegramstickies.app.ui.animatedstickereditor.AnimatedStickerEditorActivity;

public class AnimatedPackListFragment extends PackListFragment {

    private final ActivityResultLauncher<String[]> mNewPackLauncher;

    private String mPendingPackId;

    public AnimatedPackListFragment() {
        super(
                R.color.animated_theme,
                R.drawable.img_onboarding_animated,
                R.string.packlist_create_animated,
                R.string.packinformation_title_animated,
                AnimatedStickerPack.TYPE_ANIMATED_STICKER_PACK,
                AnimatedPackListAdapter.class
        );

        mNewPackLauncher = registerForActivityResult(AnimatedStickerEditorActivity.getLaunchContract(), stickerId -> {
            if (stickerId == null) {
                return;
            }

            ModelFeatures packFeatures = new ModelFeatures.Builder()
                    .add(StaticStickerPack.TYPE, AnimatedStickerPack.TYPE_ANIMATED_STICKER_PACK)
                    .add(StaticStickerPack.ID, mPendingPackId)
                    .build();

            SingleShotTarget.get(packFeatures, pack -> {
                ModelFeatures stickerFeatures = new ModelFeatures.Builder()
                        .add(Sticker.TYPE, Sticker.TYPE_STICKER)
                        .add(Sticker.ID, stickerId)
                        .build();

                pack.perform(StickerPack.ADD, stickerFeatures);
                pack.cache(true);

                SingleShotTarget.get(AnimatedStickerPackList.MODEL_FEATURES, packList -> {
                    packList.perform(AnimatedStickerPackList.ADD, packFeatures, 0);
                    packList.cache(true);
                });
            });

            startActivity(StickerPackActivity.getIntent(getContext(), AnimatedStickerPack.TYPE_ANIMATED_STICKER_PACK, mPendingPackId));
        });
    }

    @Override
    protected void onNewPackCreated(String packId) {
        mPendingPackId = packId;
        mNewPackLauncher.launch(new String[] { mPendingPackId, null});
    }

}
