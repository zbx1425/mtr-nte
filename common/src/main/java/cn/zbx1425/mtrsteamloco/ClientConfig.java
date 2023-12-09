package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class ClientConfig {

    private static Path path;

    public static boolean enableOptimization = true;
    public static boolean enableBbModelPreload = false;
    public static boolean translucentSort = false;

    public static boolean enableScriptDebugOverlay = false;

    public static boolean enableRail3D = true;
    public static boolean enableRailRender = true;
    public static boolean enableTrainRender = true;
    public static boolean enableTrainSound = true;
    public static boolean enableSmoke = true;

    public static boolean hideRidingTrain = false;

    public static void load(Path path) {
        ClientConfig.path = path;
        if (!Files.exists(path)) {
            save();
        }
        try {
            JsonObject configObject = Main.JSON_PARSER.parse(Files.readString(path)).getAsJsonObject();
            enableOptimization = !getOrDefault(configObject, "shaderCompatMode", JsonElement::getAsBoolean, false);
            enableBbModelPreload = getOrDefault(configObject, "enableBbModelPreload", JsonElement::getAsBoolean, false);
            translucentSort = getOrDefault(configObject, "translucentSort", JsonElement::getAsBoolean, false);
            enableScriptDebugOverlay = getOrDefault(configObject, "enableScriptDebugOverlay", JsonElement::getAsBoolean, false);
            enableRail3D = getOrDefault(configObject, "enableRail3D", JsonElement::getAsBoolean, true);
            enableRailRender = getOrDefault(configObject, "enableRailRender", JsonElement::getAsBoolean, true);
            enableTrainRender = getOrDefault(configObject, "enableTrainRender", JsonElement::getAsBoolean, true);
            enableTrainSound = getOrDefault(configObject, "enableTrainSound", JsonElement::getAsBoolean, true);
            enableSmoke = getOrDefault(configObject, "enableSmoke", JsonElement::getAsBoolean, true);
            hideRidingTrain = getOrDefault(configObject, "hideRidingTrain", JsonElement::getAsBoolean, false);
        } catch (Exception ex) {
            Main.LOGGER.warn("Failed loading client config:", ex);
            save();
        }
    }

    private static <T> T getOrDefault(JsonObject jsonObject, String key, Function<JsonElement, T> getter, T defaultValue) {
        if (jsonObject.has(key)) {
            return getter.apply(jsonObject.get(key));
        } else {
            return defaultValue;
        }
    }

    public static int getRailRenderLevel() {
        if (!useRenderOptimization()) {
            return enableRailRender ? 1 : 0;
        } else {
            return enableRailRender
                    ? (enableRail3D ? (ShadersModHandler.canInstance() ? 3 : 2) : 1)
                    : 0;
        }
    }

    public static boolean useRenderOptimization() {
        return enableOptimization && ShadersModHandler.canDrawWithBuffer();
    }

    public static void save() {
        try {
            if (path == null) return;
            JsonObject configObject = new JsonObject();
            configObject.addProperty("shaderCompatMode", !enableOptimization);
            configObject.addProperty("enableBbModelPreload", enableBbModelPreload);
            configObject.addProperty("translucentSort", translucentSort);
            configObject.addProperty("enableScriptDebugOverlay", enableScriptDebugOverlay);
            configObject.addProperty("enableRail3D", enableRail3D);
            configObject.addProperty("enableRailRender", enableRailRender);
            configObject.addProperty("enableTrainRender", enableTrainRender);
            configObject.addProperty("enableTrainSound", enableTrainSound);
            configObject.addProperty("enableSmoke", enableSmoke);
            configObject.addProperty("hideRidingTrain", hideRidingTrain);
            Files.writeString(path, new GsonBuilder().setPrettyPrinting().create().toJson(configObject));
        } catch (Exception ex) {
            Main.LOGGER.warn("Failed loading client config:", ex);
        }
    }

    public static void load() {
        load(Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("mtrsteamloco.json"));
    }

}
