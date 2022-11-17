package cn.zbx1425.mtrsteamloco.render.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class MtrModelRegistryUtil {

    public static ResourceManager resourceManager;

    public static JsonObject createDummyBbData(ResourceLocation actualPath) {
        JsonObject result = new JsonObject();
        result.addProperty("zbxFlag", "dummyBbData");
        result.addProperty("actualPath", actualPath.toString());
        result.add("elements", new JsonArray());
        result.add("outliner", new JsonArray());
        return result;
    }

    public static boolean isDummyBbData(JsonObject obj) {
        return obj.has("zbxFlag") && obj.get("zbxFlag").getAsString().equals("dummyBbData");
    }

    public static ResourceLocation getRlFromDummyBbData(JsonObject obj) {
        return new ResourceLocation(obj.get("actualPath").getAsString());
    }
}
