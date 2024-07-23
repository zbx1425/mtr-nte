package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mtr.mappings.Text;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

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
        List<Pair<ResourceLocation, Resource>> resources =
                MtrModelRegistryUtil.listResources(resourceManager, "mtrsteamloco", "eyecandies", ".json");
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
                MtrModelRegistryUtil.recordLoadingError("Failed loading Eye-candy " + pair.getFirst().toString(), ex);
            }
        }
    }

    public static EyeCandyProperties getProperty(String key) {
        return elements.getOrDefault(key, null);
    }

    private static EyeCandyProperties loadFromJson(ResourceManager resourceManager, String key, JsonObject obj) throws Exception {
        if (obj.has("atlasIndex")) {
            MainClient.atlasManager.load(
                    MtrModelRegistryUtil.resourceManager,  new ResourceLocation(obj.get("atlasIndex").getAsString())
            );
        }

        ModelCluster cluster = null;
        if (obj.has("model")) {
            RawModel rawModel = MainClient.modelManager.loadRawModel(resourceManager,
                    new ResourceLocation(obj.get("model").getAsString()), MainClient.atlasManager).copy();

            if (obj.has("textureId")) {
                rawModel.replaceTexture("default.png", new ResourceLocation(obj.get("textureId").getAsString()));
            }
            if (obj.has("flipV") && obj.get("flipV").getAsBoolean()) {
                rawModel.applyUVMirror(false, true);
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

            cluster = MainClient.modelManager.uploadVertArrays(rawModel);
        }
        ScriptHolder script = null;
        if (obj.has("scriptFiles")) {
            script = new ScriptHolder();
            Map<ResourceLocation, String> scripts = new Object2ObjectArrayMap<>();
            if (obj.has("scriptTexts")) {
                JsonArray scriptTexts = obj.get("scriptTexts").getAsJsonArray();
                for (int i = 0; i < scriptTexts.size(); i++) {
                    scripts.put(new ResourceLocation("mtrsteamloco", "script_texts/" + key + "/" + i),
                            scriptTexts.get(i).getAsString());
                }
            }
            JsonArray scriptFiles = obj.get("scriptFiles").getAsJsonArray();
            for (int i = 0; i < scriptFiles.size(); i++) {
                ResourceLocation scriptLocation = new ResourceLocation(scriptFiles.get(i).getAsString());
                scripts.put(scriptLocation, ResourceUtil.readResource(resourceManager, scriptLocation));
            }
            script.load("EyeCandy " + key, "Block", resourceManager, scripts);
        }
        if (cluster == null && script == null) {
            throw new IllegalArgumentException("Invalid eye-candy json: " + key);
        } else {
            return new EyeCandyProperties(Text.translatable(obj.get("name").getAsString()), cluster, script);
        }
    }
}
