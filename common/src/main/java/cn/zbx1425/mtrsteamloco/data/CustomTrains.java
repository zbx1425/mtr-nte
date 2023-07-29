package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.scripting.RenderTrainScripted;
import cn.zbx1425.mtrsteamloco.render.scripting.TrainTypeScriptContext;
import cn.zbx1425.mtrsteamloco.render.train.RenderTrainDK3;
import cn.zbx1425.mtrsteamloco.sound.BveTrainSoundFix;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mtr.client.*;
import mtr.mappings.Text;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import mtr.render.JonModelTrainRenderer;
import mtr.sound.JonTrainSound;
import mtr.sound.TrainSoundBase;
import mtr.sound.bve.BveTrainSound;
import mtr.sound.bve.BveTrainSoundConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

public class CustomTrains implements IResourcePackCreatorProperties, ICustomResources {

    public static void init(ResourceManager resourceManager) {
        readResource(resourceManager, mtr.MTR.MOD_ID + ":" + CUSTOM_RESOURCES_ID + ".json", jsonConfig -> {
            try {
                jsonConfig.get(CUSTOM_TRAINS_KEY).getAsJsonObject().entrySet().forEach(entry -> {
                    try {
                        final JsonObject jsonObject = entry.getValue().getAsJsonObject();
                        final String name = getOrDefault(jsonObject, CUSTOM_TRAINS_NAME, entry.getKey(), JsonElement::getAsString);
                        final int color = getOrDefault(jsonObject, CUSTOM_TRAINS_COLOR, 0, jsonElement -> CustomResources.colorStringToInt(jsonElement.getAsString()));
                        final String trainId = CUSTOM_TRAIN_ID_PREFIX + entry.getKey();

                        final String baseTrainType = getOrDefault(jsonObject, CUSTOM_TRAINS_BASE_TRAIN_TYPE, "", JsonElement::getAsString);
                        final TrainProperties baseTrainProperties = TrainClientRegistry.getTrainProperties(baseTrainType);
                        final String description = getOrDefault(jsonObject, CUSTOM_TRAINS_DESCRIPTION, baseTrainProperties.description, JsonElement::getAsString);
                        final String wikipediaArticle = getOrDefault(jsonObject, CUSTOM_TRAINS_WIKIPEDIA_ARTICLE, baseTrainProperties.wikipediaArticle, JsonElement::getAsString);

                        final JonTrainSound jonSoundOrDefault = baseTrainProperties.sound instanceof JonTrainSound ? (JonTrainSound) baseTrainProperties.sound : new JonTrainSound("", new JonTrainSound.JonTrainSoundConfig(null, 0, 0.5F, false, false));
                        final String baseBveSoundBaseId = baseTrainProperties.sound instanceof BveTrainSound ? ((BveTrainSound) baseTrainProperties.sound).config.baseName : "";

                        final float riderOffset = getOrDefault(jsonObject, CUSTOM_TRAINS_RIDER_OFFSET, baseTrainProperties.riderOffset, JsonElement::getAsFloat);
                        final String bveSoundBaseId = getOrDefault(jsonObject, CUSTOM_TRAINS_BVE_SOUND_BASE_ID, baseBveSoundBaseId, JsonElement::getAsString);
                        final int speedSoundCount = getOrDefault(jsonObject, CUSTOM_TRAINS_SPEED_SOUND_COUNT, jonSoundOrDefault.config.speedSoundCount, JsonElement::getAsInt);
                        final String speedSoundBaseId = getOrDefault(jsonObject, CUSTOM_TRAINS_SPEED_SOUND_BASE_ID, jonSoundOrDefault.soundId, JsonElement::getAsString);
                        final String doorSoundBaseId = getOrDefault(jsonObject, CUSTOM_TRAINS_DOOR_SOUND_BASE_ID, jonSoundOrDefault.config.doorSoundBaseId, JsonElement::getAsString);
                        final float doorCloseSoundTime = getOrDefault(jsonObject, CUSTOM_TRAINS_DOOR_CLOSE_SOUND_TIME, jonSoundOrDefault.config.doorCloseSoundTime, JsonElement::getAsFloat);
                        final boolean accelSoundAtCoast = getOrDefault(jsonObject, CUSTOM_TRAINS_ACCEL_SOUND_AT_COAST, jonSoundOrDefault.config.useAccelerationSoundsWhenCoasting, JsonElement::getAsBoolean);
                        final boolean constPlaybackSpeed = getOrDefault(jsonObject, CUSTOM_TRAINS_CONST_PLAYBACK_SPEED, jonSoundOrDefault.config.constantPlaybackSpeed, JsonElement::getAsBoolean);

                        final boolean useBveSound;
                        if (StringUtils.isEmpty(bveSoundBaseId)) {
                            useBveSound = false;
                        } else {
                            if (jsonObject.has(CUSTOM_TRAINS_BVE_SOUND_BASE_ID)) {
                                useBveSound = true;
                            } else if (jsonObject.has(CUSTOM_TRAINS_SPEED_SOUND_BASE_ID)) {
                                useBveSound = false;
                            } else {
                                useBveSound = baseTrainProperties.sound instanceof BveTrainSound;
                            }
                        }

                        ResourceLocation resourcesFile = new ResourceLocation(mtr.MTR.MOD_ID + ":" + CUSTOM_RESOURCES_ID + ".json");
                        if (jsonObject.has("script_files")) {
                            final String newBaseTrainType = jsonObject.get("baseType").getAsString().toLowerCase(Locale.ROOT);
                            TrainSoundBase trainSound = useBveSound
                                    ? new BveTrainSoundFix(new BveTrainSoundConfig(resourceManager, bveSoundBaseId))
                                    : new JonTrainSound(speedSoundBaseId, new JonTrainSound.JonTrainSoundConfig(doorSoundBaseId, speedSoundCount, doorCloseSoundTime, accelSoundAtCoast, constPlaybackSpeed));

                            TrainTypeScriptContext scriptContext = new TrainTypeScriptContext();
                            List<String> scripts = new ArrayList<>();
                            if (jsonObject.has("script_texts")) {
                                JsonArray scriptTexts = jsonObject.get("script_texts").getAsJsonArray();
                                for (int i = 0; i < scriptTexts.size(); i++) {
                                    scripts.add(scriptTexts.get(i).getAsString());
                                }
                            }
                            JsonArray scriptFiles = jsonObject.get("script_files").getAsJsonArray();
                            for (int i = 0; i < scriptFiles.size(); i++) {
                                scripts.add(ResourceUtil.readResource(resourceManager,
                                        ResourceUtil.resolveRelativePath(resourcesFile, scriptFiles.get(i).getAsString(), null)));
                            }
                            scriptContext.load(scripts.toArray(new String[]{}));

                            mtr.client.TrainClientRegistry.register(trainId, new TrainProperties(
                                    newBaseTrainType, Text.literal(name),
                                    description, wikipediaArticle, color,
                                    riderOffset, riderOffset, 0, false, false,
                                    new RenderTrainScripted(scriptContext),
                                    trainSound
                            ));
                        }
                    } catch (Exception ex) {
                        Main.LOGGER.error("Reading custom trains", ex);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Utilities.closeResource(resource);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception ignored) {
        }
    }

    private static <T> T getOrDefault(JsonObject jsonObject, String key, T defaultValue, Function<JsonElement, T> function) {
        if (jsonObject.has(key)) {
            return function.apply(jsonObject.get(key));
        } else {
            return defaultValue;
        }
    }
}
