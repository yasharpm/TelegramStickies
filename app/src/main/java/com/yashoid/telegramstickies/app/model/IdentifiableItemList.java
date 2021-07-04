package com.yashoid.telegramstickies.app.model;

import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;

import java.util.List;
import java.util.UUID;

public interface IdentifiableItemList extends ItemList, Identifiable {

    class IdentifiableItemListTypeProvider extends ItemListTypeProvider {

        public IdentifiableItemListTypeProvider(String type) {
            super(type);
        }

        public Object _model_created(Model model, Object... params) {
            super._model_created(model, params);

            if (model.get(ID) == null) {
                model.set(ID, UUID.randomUUID().toString());
            }

            return null;
        }

        @Override
        public void getIdentifyingFeatures(ModelFeatures features, List<String> identifyingFeatures) {
            super.getIdentifyingFeatures(features, identifyingFeatures);

            identifyingFeatures.add(ID);
        }

    }

}
