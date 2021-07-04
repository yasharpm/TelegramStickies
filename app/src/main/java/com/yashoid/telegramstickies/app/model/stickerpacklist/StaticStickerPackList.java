package com.yashoid.telegramstickies.app.model.stickerpacklist;

import com.yashoid.mmv.ModelFeatures;
import com.yashoid.telegramstickies.app.model.ItemList;

public interface StaticStickerPackList extends ItemList {

    String TYPE_STATIC_STICKER_PACK_LIST = "StaticStickerPackList";

    ModelFeatures MODEL_FEATURES = new ModelFeatures.Builder().add(TYPE, TYPE_STATIC_STICKER_PACK_LIST).build();

    class StaticStickerPackListTypeProvider extends ItemListTypeProvider {

        public StaticStickerPackListTypeProvider() {
            super(TYPE_STATIC_STICKER_PACK_LIST);
        }

    }

}
