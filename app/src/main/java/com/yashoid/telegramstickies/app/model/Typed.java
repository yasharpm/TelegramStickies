package com.yashoid.telegramstickies.app.model;

import com.yashoid.mmv.Action;
import com.yashoid.mmv.ModelFeatures;
import com.yashoid.mmv.TypeProvider;

import java.util.List;

public interface Typed {

    String TYPE = "type";

    abstract class TypedTypeProvider implements TypeProvider {

        private final String mType;

        public TypedTypeProvider(String type) {
            mType = type;
        }

        @Override
        public boolean isOfType(ModelFeatures features) {
            return mType.equals(features.get(TYPE));
        }

        @Override
        public Action getAction(ModelFeatures features, String actionName, Object... params) {
            return null;
        }

        @Override
        public void getIdentifyingFeatures(ModelFeatures features, List<String> identifyingFeatures) {
            identifyingFeatures.add(TYPE);
        }

    }

}
