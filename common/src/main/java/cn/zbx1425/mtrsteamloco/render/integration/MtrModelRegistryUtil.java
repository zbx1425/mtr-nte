package cn.zbx1425.mtrsteamloco.render.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.thread.BlockableEventLoop;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MtrModelRegistryUtil {

    public static ResourceManager resourceManager;

    public static final List<String> loadingErrorList = new ArrayList<>();

    public static final ResourceLocation PLACEHOLDER_TILE_TEXTURE_LOCATION = new ResourceLocation("mtrsteamloco:textures/misc/nte_tile_faded.png");

    public static void recordLoadingError(String context, Exception ex) {
        final String[] uselessPrefixes = {
                "at " + CompletableFuture.class.getName(),
                "at " + SimpleReloadInstance.class.getName(),
                "at " + BlockableEventLoop.class.getName(),
                "at java.base/jdk.internal"
        };
        String cleanedStackTrace = Arrays.stream(ExceptionUtils.getStackTrace(ex).split("\n"))
                .map(l -> l.replace("\t", "  ").replace("\r", ""))
                .filter(l -> Arrays.stream(uselessPrefixes).noneMatch(p -> l.trim().startsWith(p)))
                .takeWhile(l -> !l.trim().startsWith("at net.minecraft.client.main.Main"))
                .takeWhile(l -> !l.trim().startsWith("at com.mojang.blaze3d.systems.RenderSystem.replayQueue"))
                .map(l -> l + "\n")
                .collect(Collectors.joining()).trim();
        loadingErrorList.add(context + "\n" + cleanedStackTrace);
    }

    public static JsonObject createDummyBbDataPack(String actualPath, String textureId) {
        JsonObject result = createDummyBbData();
        result.addProperty("zbxFlag", "dummyBbData.resourceLocation");
        result.addProperty("actualPath", actualPath);
        result.addProperty("textureId", textureId);
        return result;
    }

    public static JsonObject createDummyBbDataExternal(String actualPath) {
        JsonObject result = createDummyBbData();
        result.addProperty("zbxFlag", "dummyBbData.path");
        result.addProperty("actualPath", actualPath);
        result.addProperty("textureId", "");
        return result;
    }

    private static JsonObject createDummyBbData() {
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

    public static String getPathFromDummyBbData(JsonObject obj) {
        return obj.get("actualPath").getAsString();
    }

    public static String getTextureIdFromDummyBbData(JsonObject obj) {
        return obj.get("textureId").getAsString();
    }
}
