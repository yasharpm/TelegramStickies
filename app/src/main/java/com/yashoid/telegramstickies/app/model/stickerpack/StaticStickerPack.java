package com.yashoid.telegramstickies.app.model.stickerpack;

import android.net.Uri;

import com.airbnb.lottie.LottieAnimationView;

public interface StaticStickerPack extends StickerPack {

    String TYPE_STATIC_STICKER_PACK = "StaticStickerPack";

    class StaticStickerPackTypeProvider extends StickerPackTypeProvider {

        public StaticStickerPackTypeProvider() {
            super(TYPE_STATIC_STICKER_PACK);
        }

        @Override
        protected void bindPreview(LottieAnimationView view, Uri uri) {
            view.setImageURI(uri);
        }

    }

}
