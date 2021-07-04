package com.yashoid.telegramstickies.app.model.stickerpacklist;

import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.app.model.ItemList;

public interface AnimatedStickerPackList extends ItemList {

    String TYPE_ANIMATED_STICKER_PACK_LIST = "AnimatedStickerPackList";

    ModelFeatures MODEL_FEATURES = new ModelFeatures.Builder().add(TYPE, TYPE_ANIMATED_STICKER_PACK_LIST).build();

    class AnimatedStickerPackListTypeProvider extends ItemListTypeProvider {

        public AnimatedStickerPackListTypeProvider() {
            super(TYPE_ANIMATED_STICKER_PACK_LIST);
        }

    }

}
