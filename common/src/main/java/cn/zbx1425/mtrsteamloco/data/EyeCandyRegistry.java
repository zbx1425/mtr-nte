package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import mtr.mappings.Text;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EyeCandyRegistry {

    public static Map<String, EyeCandyProperties> elements = new HashMap<>();

    public static void register(String key, EyeCandyProperties properties) {
        elements.put(key, properties);
    }

    public static void reload(ResourceManager resourceManager) {
        elements.clear();

        /*
        for (Map.Entry<ResourceLocation, ModelCluster> entry : MainClient.modelManager.uploadedVertArrays.entrySet()) {
            String key = FilenameUtils.getBaseName(entry.getKey().getPath());
            register(key, new EyeCandyProperties(Text.literal(key), entry.getValue()));
        }
        */

        List<Pair<ResourceLocation, Resource>> resources = resourceManager.listResourceStacks("eyecandies",
                rl -> rl.getNamespace().equals("mtrsteamloco") && rl.getPath().endsWith(".json"))
                .entrySet().stream().flatMap(e -> e.getValue().stream().map(r -> new Pair<>(e.getKey(), r))).toList();
        for (Pair<ResourceLocation, Resource> pair : resources) {
            try {
                try (InputStream is = Utilities.getInputStream(pair.getSecond())) {
                    JsonObject rootObj = (new JsonParser()).parse(IOUtils.toString(is, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (rootObj.has("model")) {
                        String key = FilenameUtils.getBaseName(pair.getFirst().getPath());
                        register(key, loadFromJson(resourceManager, rootObj));
                    } else {
                        for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            String key = entry.getKey().toLowerCase(Locale.ROOT);
                            register(key, loadFromJson(resourceManager, obj));
                        }
                    }
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Failed loading eye-candy: " + pair.getFirst().toString(), ex);
                MtrModelRegistryUtil.loadingErrorList.add("Eye-candy " + pair.getFirst().toString()
                        + ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    public static ModelCluster getModel(String key) {
        if (elements.containsKey(key)) {
            return elements.get(key).model;
        } else {
            return null;
        }
    }

    private static EyeCandyProperties loadFromJson(ResourceManager resourceManager, JsonObject obj) throws IOException {
        if (obj.has("atlasIndex")) {
            MainClient.atlasManager.load(
                    MtrModelRegistryUtil.resourceManager,  new ResourceLocation(obj.get("atlasIndex").getAsString())
            );
        }

        RawModel rawModel = MainClient.modelManager.loadRawModel(resourceManager,
                new ResourceLocation(obj.get("model").getAsString()), MainClient.atlasManager);

        if (obj.has("textureId")) {
            rawModel.replaceAllTexture("default.png", new ResourceLocation(obj.get("textureId").getAsString()));
        }

        ModelCluster cluster = MainClient.modelManager.uploadVertArrays(rawModel);

        return new EyeCandyProperties(Text.translatable(obj.get("name").getAsString()), cluster);
    }
}
