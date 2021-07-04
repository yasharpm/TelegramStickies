package com.yashoid.telegramstickies.app.model;

import com.yashoid.mmv.Model;
import com.yashoid.mmv.ModelFeatures;

import java.util.ArrayList;
import java.util.List;

public interface ItemList extends Typed {

    String ITEMS = "items";

    String ADD = "add";

    abstract class ItemListTypeProvider extends TypedTypeProvider {

        public ItemListTypeProvider(String type) {
            super(type);
        }

        public Object _model_created(Model model, Object... params) {
            model.set(ITEMS, new ArrayList<>());

            return null;
        }

        public Object add(Model model, Object... params) {
            ArrayList<ModelFeatures> items = model.get(ITEMS);

            int index = params.length > 1 ? (int) params[1] : items.size();

            items.add(index, (ModelFeatures) params[0]);

            model.set(ITEMS, items);
            return null;
        }

    }

}
