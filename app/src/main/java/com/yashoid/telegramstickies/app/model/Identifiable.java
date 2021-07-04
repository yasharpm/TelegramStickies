package com.yashoid.telegramstickies.app.model;

import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;

import java.util.List;
import java.util.UUID;

public interface Identifiable extends Typed {

    String ID = "id";

    abstract class IdentifiableTypeProvider extends TypedTypeProvider {

        public IdentifiableTypeProvider(String type) {
            super(type);
        }

        public Object _model_created(Model model, Object... params) {
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
