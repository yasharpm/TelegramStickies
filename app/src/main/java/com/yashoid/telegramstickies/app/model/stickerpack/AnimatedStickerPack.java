package com.yashoid.telegramstickies.app.model.stickerpack;

import android.net.Uri;
import android.widget.ImageView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.yashoid.telegramstickies.app.Utils;

import java.io.FileNotFoundException;

public interface AnimatedStickerPack extends StickerPack {

    String TYPE_ANIMATED_STICKER_PACK = "AnimatedStickerPack";

    class AnimatedStickerPackTypeProvider extends StickerPackTypeProvider {

        public AnimatedStickerPackTypeProvider() {
            super(TYPE_ANIMATED_STICKER_PACK);
        }

        @Override
        protected void bindPreview(LottieAnimationView view, Uri uri) {
            try {
                view.setRepeatMode(LottieDrawable.RESTART);
                view.setRepeatCount(LottieDrawable.INFINITE);
                view.setAnimation(view.getContext().getContentResolver().openInputStream(uri), uri.toString());
                view.playAnimation();
            } catch (Throwable t) { }
        }

    }

}
