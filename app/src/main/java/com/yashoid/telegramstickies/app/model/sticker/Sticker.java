package com.yashoid.telegramstickies.app.model.sticker;

import com.yashoid.telegramstickies.app.model.Identifiable;

public interface Sticker extends Identifiable {

    String TYPE_STICKER = "Sticker";

    String IMAGE_FILE = "image_file";
    String EMOJIS = "emojis";

    class StickerTypeProvider extends IdentifiableTypeProvider {

        public StickerTypeProvider() {
            super(TYPE_STICKER);
        }
    }

}
