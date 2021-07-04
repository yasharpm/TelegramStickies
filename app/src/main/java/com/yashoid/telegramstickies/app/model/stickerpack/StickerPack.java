package com.yashoid.telegramstickies.app.model.stickerpack;

import android.net.Uri;

import com.airbnb.lottie.LottieAnimationView;
import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.app.model.Identifiable;
import com.yashoid.telegramstickies.app.model.IdentifiableItemList;
import com.yashoid.telegramstickies.app.model.Typed;

import java.util.List;
import java.util.UUID;

public interface StickerPack extends IdentifiableItemList {

    String NAME = "name";
    String CREATOR = "creator";

    String BIND_PREVIEW = "bindPreview";

    abstract class StickerPackTypeProvider extends IdentifiableItemListTypeProvider {

        public StickerPackTypeProvider(String type) {
            super(type);
        }

        public Object bindPreview(Model sticker, Object... params) {
            bindPreview((LottieAnimationView) params[0], (Uri) params[1]);

            return null;
        }

        abstract protected void bindPreview(LottieAnimationView view, Uri uri);

    }

}
