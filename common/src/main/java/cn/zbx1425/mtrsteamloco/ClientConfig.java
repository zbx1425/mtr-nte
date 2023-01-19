package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import cn.zbx1425.sowcer.ContextCapability;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class ClientConfig {

    private static Path path;

    public static boolean shaderCompatMode = false;
    public static boolean enableRail3D = true;
    public static boolean enableRailRender = true;
    public static boolean enableTrainRender = true;
    public static boolean enableSmoke = true;

    public static boolean hideRidingTrain = false;

    public static void load(Path path) {
        ClientConfig.path = path;
        try {
            JsonObject configObject = Main.JSON_PARSER.parse(Files.readString(path)).getAsJsonObject();
            shaderCompatMode = configObject.get("shaderCompatMode").getAsBoolean();
            enableRail3D = configObject.get("enableRail3D").getAsBoolean();
            enableRailRender = configObject.get("enableRailRender").getAsBoolean();
            enableTrainRender = configObject.get("enableTrainRender").getAsBoolean();
            enableSmoke = configObject.get("enableSmoke").getAsBoolean();
            hideRidingTrain = configObject.get("hideRidingTrain").getAsBoolean();
        } catch (Exception ex) {
            Main.LOGGER.warn("Failed loading client config:", ex);
            ex.printStackTrace();
            save();
        }
    }

    public static int getRailRenderLevel() {
        if (shaderCompatMode || ShadersModHandler.isShaderPackInUse() || !ContextCapability.supportVertexAttribDivisor) {
            return enableRailRender ? 1 : 0;
        } else {
            return enableRailRender
                    ? (enableRail3D ? 2 : 1)
                    : 0;
        }
    }

    public static boolean useRenderOptimization() {
        return !shaderCompatMode;
    }

    public static void save() {
        try {
            if (path == null) return;
            JsonObject configObject = new JsonObject();
            configObject.addProperty("shaderCompatMode", shaderCompatMode);
            configObject.addProperty("enableRail3D", enableRail3D);
            configObject.addProperty("enableRailRender", enableRailRender);
            configObject.addProperty("enableTrainRender", enableTrainRender);
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
