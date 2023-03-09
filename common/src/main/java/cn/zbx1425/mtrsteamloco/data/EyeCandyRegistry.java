package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import com.google.gson.JsonArray;
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

#if MC_VERSION >= "11900"
        List<Pair<ResourceLocation, Resource>> resources = resourceManager.listResourceStacks("eyecandies",
                rl -> rl.getNamespace().equals("mtrsteamloco") && rl.getPath().endsWith(".json"))
                .entrySet().stream().flatMap(e -> e.getValue().stream().map(r -> new Pair<>(e.getKey(), r))).toList();
#else
        List<Pair<ResourceLocation, Resource>> resources = resourceManager.listResources("eyecandies", rl -> rl.endsWith(".json"))
                .stream().filter(rl -> rl.getNamespace().equals("mtrsteamloco")).flatMap(rl -> {
                    try {
                        return resourceManager.getResources(rl).stream().map(r -> new Pair<>(rl, r));
                    } catch (IOException e) {
                        return java.util.stream.Stream.of();
                    }
                }).toList();
#endif
        for (Pair<ResourceLocation, Resource> pair : resources) {
            try {
                try (InputStream is = Utilities.getInputStream(pair.getSecond())) {
                    JsonObject rootObj = (new JsonParser()).parse(IOUtils.toString(is, StandardCharsets.UTF_8)).getAsJsonObject();
                    if (rootObj.has("model")) {
                        String key = FilenameUtils.getBaseName(pair.getFirst().getPath());
                        register(key, loadFromJson(resourceManager, key, rootObj));
                    } else {
                        for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                            JsonObject obj = entry.getValue().getAsJsonObject();
                            String key = entry.getKey().toLowerCase(Locale.ROOT);
                            register(key, loadFromJson(resourceManager, key, obj));
                        }
                    }
                }
            } catch (Exception ex) {
                Main.LOGGER.error("Failed loading eye-candy: " + pair.getFirst().toString(), ex);
                MtrModelRegistryUtil.recordLoadingError("Eye-candy " + pair.getFirst().toString(), ex);
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

    private static EyeCandyProperties loadFromJson(ResourceManager resourceManager, String key, JsonObject obj) throws IOException {
        if (obj.has("atlasIndex")) {
            MainClient.atlasManager.load(
                    MtrModelRegistryUtil.resourceManager,  new ResourceLocation(obj.get("atlasIndex").getAsString())
            );
        }

        RawModel rawModel = MainClient.modelManager.loadRawModel(resourceManager,
                new ResourceLocation(obj.get("model").getAsString()), MainClient.atlasManager).copy();

        if (obj.has("textureId")) {
            rawModel.replaceAllTexture("default.png", new ResourceLocation(obj.get("textureId").getAsString()));
        }

        if (obj.has("translation")) {
            JsonArray vec = obj.get("translation").getAsJsonArray();
            rawModel.applyTranslation(vec.get(0).getAsFloat(), vec.get(1).getAsFloat(), vec.get(2).getAsFloat());
        }
        if (obj.has("rotation")) {
            JsonArray vec = obj.get("rotation").getAsJsonArray();
            rawModel.applyRotation(new Vector3f(1, 0, 0), vec.get(0).getAsFloat());
            rawModel.applyRotation(new Vector3f(0, 1, 0), vec.get(1).getAsFloat());
            rawModel.applyRotation(new Vector3f(0, 0, 1), vec.get(2).getAsFloat());
        }
        if (obj.has("scale")) {
            JsonArray vec = obj.get("scale").getAsJsonArray();
            rawModel.applyScale(vec.get(0).getAsFloat(), vec.get(1).getAsFloat(), vec.get(2).getAsFloat());
        }
        if (obj.has("mirror")) {
            JsonArray vec = obj.get("mirror").getAsJsonArray();
            rawModel.applyMirror(
                    vec.get(0).getAsBoolean(), vec.get(1).getAsBoolean(), vec.get(2).getAsBoolean(),
                    vec.get(0).getAsBoolean(), vec.get(1).getAsBoolean(), vec.get(2).getAsBoolean()
            );
        }

        rawModel.sourceLocation = new ResourceLocation(rawModel.sourceLocation.toString() + "/" + key);

        ModelCluster cluster = MainClient.modelManager.uploadVertArrays(rawModel);

        return new EyeCandyProperties(Text.translatable(obj.get("name").getAsString()), cluster);
    }
}
