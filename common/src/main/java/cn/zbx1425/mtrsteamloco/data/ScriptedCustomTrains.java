package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.train.ScriptedTrainRenderer;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mtr.client.*;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import mtr.render.TrainRendererBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScriptedCustomTrains implements IResourcePackCreatorProperties, ICustomResources {

    public static void init(ResourceManager resourceManager) {
        readResource(resourceManager, mtr.MTR.MOD_ID + ":" + CUSTOM_RESOURCES_ID + ".json", jsonConfig -> {
            try {
                jsonConfig.get(CUSTOM_TRAINS_KEY).getAsJsonObject().entrySet().forEach(entry -> {
                    try {
                        final JsonObject jsonObject = entry.getValue().getAsJsonObject();
                        if (!(jsonObject.has("script_files") || jsonObject.has("script_texts"))) return;
                        final String trainId = CUSTOM_TRAIN_ID_PREFIX + entry.getKey();

                        TrainProperties prevTrainProp = TrainClientRegistry.getTrainProperties(trainId);
                        if (prevTrainProp.baseTrainType.isEmpty()) return;

                        final boolean isJacobsBogie = getOrDefault(jsonObject, "is_jacobs_bogie", prevTrainProp.isJacobsBogie, JsonElement::getAsBoolean);
                        final float bogiePosition = getOrDefault(jsonObject, "bogie_position", prevTrainProp.bogiePosition, JsonElement::getAsFloat);

                        ScriptHolder scriptContext = new ScriptHolder();
                        Map<ResourceLocation, String> scripts = new Object2ObjectArrayMap<>();
                        if (jsonObject.has("script_texts")) {
                            JsonArray scriptTexts = jsonObject.get("script_texts").getAsJsonArray();
                            for (int i = 0; i < scriptTexts.size(); i++) {
                                scripts.put(new ResourceLocation("mtrsteamloco", "script_texts/" + trainId + "/" + i),
                                        scriptTexts.get(i).getAsString());
                            }
                        }
                        if (jsonObject.has("script_files")) {
                            JsonArray scriptFiles = jsonObject.get("script_files").getAsJsonArray();
                            for (int i = 0; i < scriptFiles.size(); i++) {
                                ResourceLocation scriptLocation = new ResourceLocation(scriptFiles.get(i).getAsString());
                                scripts.put(scriptLocation, null);
                            }
                        }
                        scriptContext.load("Train " + entry.getKey(), "Train", resourceManager, scripts);

                        boolean dummyBaseTrain = jsonObject.has("base_type");
                        String baseTrainType = dummyBaseTrain ? jsonObject.get("base_type").getAsString() : prevTrainProp.baseTrainType;
                        boolean hasGangwayConnection = getOrDefault(jsonObject, "has_gangway_connection",
                                dummyBaseTrain || prevTrainProp.hasGangwayConnection, JsonElement::getAsBoolean);
                        TrainRendererBase newRenderer = new ScriptedTrainRenderer(scriptContext, dummyBaseTrain ? null : prevTrainProp.renderer);

                        mtr.client.TrainClientRegistry.register(trainId, new TrainProperties(
                                baseTrainType, prevTrainProp.name,
                                prevTrainProp.description, prevTrainProp.wikipediaArticle, prevTrainProp.color,
                                prevTrainProp.riderOffset, prevTrainProp.riderOffsetDismounting,
                                bogiePosition, isJacobsBogie, hasGangwayConnection,
                                newRenderer, prevTrainProp.sound
                        ));
                    } catch (Exception ex) {
                        Main.LOGGER.error("Reading scripted custom train", ex);
                        MtrModelRegistryUtil.recordLoadingError("Failed loading Scripted Custom Train", ex);
                    }
                });
            } catch (Exception ignored) {
            }
        });

    }

    private static void readResource(ResourceManager manager, String path, Consumer<JsonObject> callback) {
        try {
            UtilitiesClient.getResources(manager, new ResourceLocation(path)).forEach(resource -> {
                try (final InputStream stream = Utilities.getInputStream(resource)) {
                    callback.accept(new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
                } catch (Exception e) { Main.LOGGER.error("On behalf of MTR: Parsing JSON " + path, e); }
                try {
                    Utilities.closeResource(resource);
                } catch (IOException e) { Main.LOGGER.error("On behalf of MTR: Closing resource " + path, e); }
            });
        } catch (Exception ignored) { }
    }

    private static <T> T getOrDefault(JsonObject jsonObject, String key, T defaultValue, Function<JsonElement, T> function) {
        if (jsonObject.has(key)) {
            return function.apply(jsonObject.get(key));
        } else {
            return defaultValue;
        }
    }
}
