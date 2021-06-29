package com.yashoid.telegramstickies;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * https://github.com/airbnb/lottie-web/tree/master/docs/json
 */
public class ParsedSticker {

    private static final String WIDTH = "w";
    private static final String HEIGHT = "h";
    private static final String IN_POINT = "ip";
    private static final String OUT_POINT = "op";
    private static final String LAYERS = "layers";
    private static final String ASSETS = "assets";

    private static final String LAYER_TYPE = "ty";
    private static final String LAYER_TRANSFORM = "ks";
    private static final String LAYER_INDEX = "ind";
    private static final String LAYER_IN_POINT = "ip";
    private static final String LAYER_OUT_POINT = "op";
    private static final String LAYER_START = "st";
    private static final String LAYER_TIME_STRETCHING = "sr";
    private static final String LAYER_PARENT = "parent";
    private static final String LAYER_SHAPES = "shapes";

    private static final int LAYER_TYPE_SOLID = 1;
    private static final int LAYER_TYPE_NULL = 3;

    private static final String TRANSFORM_ANCHOR_POINT = "a"; // {"a":0, "k":[0, 0, 0]}
    private static final String TRANSFORM_POSITION = "p"; // {"a":0, "k":[0, 0, 0]}
    private static final String TRANSFORM_SCALE = "s"; // {"a":0, "k":[100, 100, 100]}
    private static final String TRANSFORM_ROTATION = "r"; // {"a":0, "k":0}
    private static final String TRANSFORM_OPACITY = "o"; // {"a":0, "k":100}
    private static final String TRANSFORM_POSITION_X = "px"; // {"a":0, "k":0}
    private static final String TRANSFORM_POSITION_Y = "py"; // {"a":0, "k":0}
    private static final String TRANSFORM_POSITION_Z = "pz"; // {"a":0, "k":0}
    private static final String TRANSFORM_SKEW = "sk"; // {"a":0, "k":0}
    private static final String TRANSFORM_SLEW_AXIS = "sa"; // {"a":0, "k":0}

    private static final String SHAPE_TYPE = "ty";

    private static final String SOLID_COLOR = "c";
    private static final String GRADIENT_COLOR = "g";
    private static final String GROUP_ITEMS = "it";

    private static final String SHAPE_TYPE_FILL = "fl";
    private static final String SHAPE_TYPE_STROKE = "st";
    private static final String SHAPE_TYPE_GRADIENT = "gf"; // [position, r, g, b][position, alpha]
    private static final String SHAPE_TYPE_GRADIENT_STROKE = "gs";
    private static final String SHAPE_TYPE_GROUP = "gr";

    public static void updateBody(JSONObject body, float width, float height, int duration) throws JSONException {
        body.put(WIDTH, width);
        body.put(HEIGHT, height);
        body.put(OUT_POINT, duration);
    }

    public static void apply(JSONObject src, JSONObject dst, int timeShift, float timeScale,
                             float left, float scaleX, float top, float scaleY, float rotation,
                             Map<StickerColor, StickerColor> colorReplacements) throws JSONException {
        int srcDuration = src.getInt(OUT_POINT);
        int dstDuration = dst.getInt(OUT_POINT);

        JSONArray srcAssets = src.getJSONArray(ASSETS);
        JSONArray dstAssets = dst.getJSONArray(ASSETS);

        for (int i = 0; i < srcAssets.length(); i++) {
            dstAssets.put(srcAssets.get(i));
        }

        JSONArray srcLayers = src.getJSONArray(LAYERS);
        JSONArray dstLayers = dst.getJSONArray(LAYERS);

        int indexOffset = dstLayers.length() + 1;

        JSONObject transformationLayer = new JSONObject();
        transformationLayer.put(LAYER_TYPE, LAYER_TYPE_NULL);
        transformationLayer.put(LAYER_INDEX, indexOffset);
        transformationLayer.put(LAYER_IN_POINT, 0);
        transformationLayer.put(LAYER_OUT_POINT, dstDuration);
        transformationLayer.put(LAYER_START, 0);
        transformationLayer.put(LAYER_TIME_STRETCHING, 1);

        JSONObject transformation = new JSONObject();
//        transformation.put(TRANSFORM_ANCHOR_POINT, createValue((float) src.getDouble(WIDTH) / 2f, (float) src.getDouble(HEIGHT) / 2f, 0));
        transformation.put(TRANSFORM_ROTATION, createValue(rotation));
        transformation.put(TRANSFORM_SCALE, createValue(scaleX * 100f, scaleY * 100f, 100));
        transformation.put(TRANSFORM_POSITION, createValue(left, top, 0));

        transformationLayer.put(LAYER_TRANSFORM, transformation);
        dstLayers.put(transformationLayer);

        for (int i = 0; i < srcLayers.length(); i++) {
            JSONObject layer = copyJSON(srcLayers.getJSONObject(i));

            layer.put(LAYER_INDEX, indexOffset + layer.getInt(LAYER_INDEX));

            if (!layer.has(LAYER_PARENT)) {
                layer.put(LAYER_PARENT, indexOffset);
            }
            else {
                layer.put(LAYER_PARENT, indexOffset + layer.getInt(LAYER_PARENT));
            }

            if (srcDuration == layer.getInt(LAYER_OUT_POINT)) {
                layer.put(LAYER_OUT_POINT, dstDuration);
            }

            // TODO
//            if (timeShift != 0) {
//                layer.put(LAYER_IN_POINT, timeShift + layer.getInt(LAYER_IN_POINT));
//                layer.put(LAYER_OUT_POINT, timeShift + layer.getInt(LAYER_OUT_POINT));
//                layer.put(LAYER_START, timeShift + layer.getInt(LAYER_START));
//            }
//
//            if (timeScale != 1) {
//                layer.put(LAYER_TIME_STRETCHING, timeScale * layer.getDouble(LAYER_TIME_STRETCHING));
//            }

            if (!colorReplacements.isEmpty() && layer.has(LAYER_SHAPES)) {
                JSONArray shapes = layer.getJSONArray(LAYER_SHAPES);

                for (int j = 0; j < shapes.length(); j++) {
                    JSONObject shape = copyJSON(shapes.getJSONObject(j));
                    replaceColors(shape, colorReplacements);
                    shapes.put(j, shape);
                }
            }

            dstLayers.put(layer);
        }
    }

    private static void replaceColors(JSONObject shape, Map<StickerColor, StickerColor> colorReplacements) throws JSONException {
        switch (shape.getString(SHAPE_TYPE)) {
            case SHAPE_TYPE_FILL:
            case SHAPE_TYPE_STROKE:
                StickerColor color = new StickerColor(readValue(shape.getJSONObject(SOLID_COLOR)));

                if (colorReplacements.containsKey(color)) {
                    shape.put(SOLID_COLOR, createValue(colorReplacements.get(color).rawValue()));
                }
                break;
            case SHAPE_TYPE_GRADIENT:
            case SHAPE_TYPE_GRADIENT_STROKE:
                JSONObject gradient = copyJSON(shape.getJSONObject(GRADIENT_COLOR));
                float[] values = readValue(gradient.getJSONObject("k"));
                int points = gradient.has("p") ? gradient.getInt("p") : values.length / 4;
                replaceGradientColors(points, values, colorReplacements);
                gradient.put("k", createValue(values));
                break;
            case SHAPE_TYPE_GROUP:
                JSONArray children = shape.getJSONArray(GROUP_ITEMS);

                for (int i = 0; i < children.length(); i++) {
                    JSONObject child = copyJSON(children.getJSONObject(i));
                    replaceColors(child, colorReplacements);
                    children.put(i, child);
                }
                break;
        }
    }

    private static void replaceGradientColors(int points, float[] values, Map<StickerColor, StickerColor> colorReplacements) {
        for (int i = 0; i < points; i++) {
            StickerColor color = new StickerColor(values[i * 4 + 1], values[i * 4 + 2], values[i * 4 + 3], 1);
            StickerColor replacementColor = colorReplacements.get(color);

            if (replacementColor != null) {
                values[i * 4 + 1] = replacementColor.rawValue()[0];
                values[i * 4 + 2] = replacementColor.rawValue()[1];
                values[i * 4 + 3] = replacementColor.rawValue()[2];
            }
        }
    }

    private final JSONObject mBody;
    private final List<JSONObject> mLayers;
    private final List<JSONObject> mAssets;

    public ParsedSticker(String str) throws JSONException {
        mBody = new JSONObject(str);

        JSONArray jLayers = mBody.getJSONArray(LAYERS);

        mLayers = new ArrayList<>(jLayers.length());

        for (int i = 0; i < jLayers.length(); i++) {
            mLayers.add(jLayers.getJSONObject(i));
        }

        JSONArray jAssets = mBody.getJSONArray(ASSETS);

        mAssets = new ArrayList<>(jAssets.length());

        for (int i = 0; i < jAssets.length(); i++) {
            mAssets.add(jAssets.getJSONObject(i));
        }
    }

    public int getDuration() throws JSONException {
        return mBody.getInt(OUT_POINT);
    }

    public float getWidth() throws JSONException {
        return (float) mBody.getDouble(WIDTH);
    }

    public float getHeight() throws JSONException {
        return (float) mBody.getDouble(HEIGHT);
    }

    public Set<StickerColor> getColors() throws JSONException {
        Set<StickerColor> colors = new HashSet<>();

        for (JSONObject layer: mLayers) {
            if (layer.has(LAYER_SHAPES)) {
                JSONArray shapes = layer.getJSONArray(LAYER_SHAPES);

                for (int i = 0; i < shapes.length(); i++) {
                    extractShapeColors(shapes.getJSONObject(i), colors);
                }
            }
        }

        return colors;
    }

    private void extractShapeColors(JSONObject shape, Set<StickerColor> colors) throws JSONException {
        String shapeType = shape.getString(SHAPE_TYPE);

        switch (shapeType) {
            case SHAPE_TYPE_FILL:
            case SHAPE_TYPE_STROKE:
                colors.add(new StickerColor(readValue(shape.getJSONObject(SOLID_COLOR))));
                break;
            case SHAPE_TYPE_GRADIENT:
            case SHAPE_TYPE_GRADIENT_STROKE:
                JSONObject gradient = shape.getJSONObject(GRADIENT_COLOR);

                int points = gradient.has("p") ? gradient.getInt("p") : -1;
                float[] values = readValue(gradient.getJSONObject("k"));

                if (points == -1) {
                    points = values.length / 4;
                }

                for (int i = 0; i < points; i++) {
                    colors.add(new StickerColor(values[i * 4 + 1], values[i * 4 + 2], values[i * 4 + 3], 1));
                }
                break;
            case SHAPE_TYPE_GROUP:
                JSONArray shapes = shape.getJSONArray(GROUP_ITEMS);

                for (int i = 0; i < shapes.length(); i++) {
                    extractShapeColors(shapes.getJSONObject(i), colors);
                }
                break;
        }
    }

    public void apply(JSONObject dst, int timeShift, float timeScale, float left, float scaleX, float top, float scaleY, float rotation, Map<StickerColor, StickerColor> colorReplacements) throws JSONException {
        apply(mBody, dst, timeShift, timeScale, left, scaleX, top, scaleY, rotation, colorReplacements);
    }

    static JSONObject createValue(float... values) {
        JSONObject json = new JSONObject();

        try {
            json.put("a", 0);

            if (values.length == 1) {
                json.put("k", values[0]);
            }
            else {
                JSONArray jValues = new JSONArray();

                for (float value: values) {
                    jValues.put(value);
                }

                json.put("k", jValues);
            }
        } catch (JSONException e) { }

        return json;
    }

    static float[] readValue(JSONObject json) throws JSONException {
        Object rawValues = json.get("k");

        if (rawValues instanceof JSONArray) {
            JSONArray jValues = (JSONArray) rawValues;

            float[] values = new float[jValues.length()];

            for (int i = 0; i < jValues.length(); i++) {
                values[i] = (float) jValues.getDouble(i);
            }

            return values;
        }
        else {
            return new float[] { Float.parseFloat(rawValues.toString()) };
        }
    }

    static JSONObject copyJSON(JSONObject src) throws JSONException {
        List<String> names = new LinkedList<>();

        Iterator<String> namesIterator = src.keys();

        while (namesIterator.hasNext()) {
            names.add(namesIterator.next());
        }

        return new JSONObject(src, names.toArray(new String[0]));
    }

}
