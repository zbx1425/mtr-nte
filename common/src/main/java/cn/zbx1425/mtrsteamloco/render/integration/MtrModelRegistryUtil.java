package cn.zbx1425.mtrsteamloco.render.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.packs.resources.ResourceManager;

public class MtrModelRegistryUtil {

    public static ResourceManager resourceManager;

    public static JsonObject createDummyBbDataWithRl(String actualPath) {
        JsonObject result = createDummyBbDataWithPath();
        result.addProperty("zbxFlag", "dummyBbData.resourceLocation");
        result.addProperty("actualResourceLocation", actualPath);
        return result;
    }

    public static JsonObject createDummyBbDataWithPath(String actualPath) {
        JsonObject result = createDummyBbDataWithPath();
        result.addProperty("zbxFlag", "dummyBbData.path");
        result.addProperty("actualPath", actualPath);
        return result;
    }

    private static JsonObject createDummyBbDataWithPath() {
        JsonObject result = new JsonObject();
        result.add("elements", new JsonArray());
        result.add("outliner", new JsonArray());
        JsonObject resolution = new JsonObject();
        resolution.addProperty("width", 0);
        resolution.addProperty("height", 0);
        result.add("resolution", resolution);
        return result;
    }

    public static int getDummyBbDataType(JsonObject obj) {
        if (!obj.has("zbxFlag")) return 0;
        if (obj.get("zbxFlag").getAsString().equals("dummyBbData.resourceLocation")) return 1;
        if (obj.get("zbxFlag").getAsString().equals("dummyBbData.path")) return 2;
        return 0;
    }

    public static boolean isDummyBbData(JsonObject obj) {
        return getDummyBbDataType(obj) > 0;
    }

    public static String getRlFromDummyBbData(JsonObject obj) {
        return obj.get("actualResourceLocation").getAsString();
    }

    public static String getPathFromDummyBbData(JsonObject obj) {
        return obj.get("actualPath").getAsString();
    }
}
