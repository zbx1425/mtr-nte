package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import mtr.client.DynamicTrainModel;
import mtr.client.IResourcePackCreatorProperties;
import mtr.client.ResourcePackCreatorProperties;
import mtr.data.EnumHelper;
import mtr.model.ModelTrainBase;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.util.*;

public class DynamicTrainModelLoader {

    private static Map<String, RawModel> cachedModels;
    private static String cachedPath;
    private static long cachedPathMtime = 0;

    public static void loadInto(JsonObject model, DynamicTrainModel target) {
        if (MtrModelRegistryUtil.isDummyBbData(model)) {
            int bbDataType = MtrModelRegistryUtil.getDummyBbDataType(model);
            target.parts.clear();
            try {
                if (target.properties.has("atlasIndex")) {
                    MainClient.atlasManager.load(
                            MtrModelRegistryUtil.resourceManager,
                            new ResourceLocation(target.properties.get("atlasIndex").getAsString())
                    );
                }

                Map<String, RawModel> models = new HashMap<>();
                if (bbDataType == 1) {
                    String modelLocations = MtrModelRegistryUtil.getPathFromDummyBbData(model);
                    if (modelLocations.contains("|")) {
                        String[] rlListPairs = modelLocations.split("\\|");
                        ArrayList<JsonObject> previousParts = new ArrayList<>();
                        target.properties.get(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS).getAsJsonArray()
                                .forEach(elem -> previousParts.add(elem.getAsJsonObject()));
                        JsonArray newParts = new JsonArray();
                        for (int i = 0; i < rlListPairs.length / 2; i++) {
                            ResourceLocation modelLocation = new ResourceLocation(rlListPairs[i * 2]);
                            String[] extraAttribs = rlListPairs[i * 2 + 1].split(";", -1)[2].split(",");
                            boolean isModelReversed = Arrays.asList(extraAttribs).contains("reversed");
                            String modelLocationName = modelLocation.getPath().substring(modelLocation.getPath().lastIndexOf('/') + 1)
                                    + (isModelReversed ? "/reversed" : "");

                            // Load models and register parts under alias
                            Map<String, RawModel> modelParts = ObjModelLoader.loadModels(
                                    MtrModelRegistryUtil.resourceManager,
                                    modelLocation,
                                    MainClient.atlasManager
                            );
                            for (Map.Entry<String, RawModel> entry : modelParts.entrySet()) {
                                if (isModelReversed) {
                                    entry.getValue().sourceLocation = new ResourceLocation(
                                            entry.getValue().sourceLocation.toString().substring(0, entry.getValue().sourceLocation.toString().lastIndexOf("/"))
                                                    + "/reversed"
                                                    + entry.getValue().sourceLocation.toString().substring(entry.getValue().sourceLocation.toString().lastIndexOf("/"))
                                    );
                                    entry.getValue().applyRotation(new Vector3f(0, 1, 0), 180);
                                }
                                models.put(modelLocationName + "/" + entry.getKey(), entry.getValue());
                            }

                            // Don't process part definitions specially targeting parts in a single model file
                            previousParts.forEach(elem -> {
                                if (elem.get("name").getAsString().startsWith(modelLocationName)) {
                                    newParts.add(elem);
                                }
                            });
                            previousParts.removeIf(elem -> elem.get("name").getAsString().startsWith(modelLocationName));
                        }
                        for (int i = 0; i < rlListPairs.length / 2; i++) {
                            ResourceLocation modelLocation = new ResourceLocation(rlListPairs[i * 2]);
                            String[] extraAttribs = rlListPairs[i * 2 + 1].split(";", -1)[2].split(",");
                            boolean isModelReversed = Arrays.asList(extraAttribs).contains("reversed");
                            String modelLocationName = modelLocation.getPath().substring(modelLocation.getPath().lastIndexOf('/') + 1)
                                    + (isModelReversed ? "/reversed" : "");String whiteList = rlListPairs[i * 2 + 1].split(";", -1)[0];
                            String blackList = rlListPairs[i * 2 + 1].split(";", -1)[1];

                            // Apply general parts for each sub model
                            for (JsonObject part : previousParts) {
                                JsonObject newPartObj = (JsonObject)(new JsonParser()).parse(part.toString());
                                String previousName = newPartObj.get("name").getAsString();
                                newPartObj.remove("name");
                                newPartObj.addProperty("name", modelLocationName + "/" + previousName);
                                newPartObj.remove("whitelisted_cars");
                                newPartObj.addProperty("whitelisted_cars", whiteList);
                                newPartObj.remove("blacklisted_cars");
                                newPartObj.addProperty("blacklisted_cars", blackList);

                                if (isModelReversed) {
                                    // Reverse door commands
                                    ResourcePackCreatorProperties.DoorOffset newDoorOffset =
                                            EnumHelper.valueOf(ResourcePackCreatorProperties.DoorOffset.NONE, newPartObj.get("door_offset").getAsString().toUpperCase(Locale.ROOT));
                                    switch (newDoorOffset) {
                                        case LEFT_POSITIVE:
                                            newDoorOffset = ResourcePackCreatorProperties.DoorOffset.RIGHT_NEGATIVE;
                                            break;
                                        case RIGHT_POSITIVE:
                                            newDoorOffset = ResourcePackCreatorProperties.DoorOffset.LEFT_NEGATIVE;
                                            break;
                                        case LEFT_NEGATIVE:
                                            newDoorOffset = ResourcePackCreatorProperties.DoorOffset.RIGHT_POSITIVE;
                                            break;
                                        case RIGHT_NEGATIVE:
                                            newDoorOffset = ResourcePackCreatorProperties.DoorOffset.LEFT_POSITIVE;
                                            break;
                                    }
                                    newPartObj.remove("door_offset");
                                    newPartObj.addProperty("door_offset", newDoorOffset.toString());
                                    // Reverse render conditions
                                    ResourcePackCreatorProperties.RenderCondition newRenderCondition =
                                            EnumHelper.valueOf(ResourcePackCreatorProperties.RenderCondition.ALL, newPartObj.get("render_condition").getAsString().toUpperCase(Locale.ROOT));
                                    switch (newRenderCondition) {
                                        case DOOR_LEFT_OPEN:
                                            newRenderCondition = ResourcePackCreatorProperties.RenderCondition.DOOR_RIGHT_OPEN;
                                            break;
                                        case DOOR_RIGHT_OPEN:
                                            newRenderCondition = ResourcePackCreatorProperties.RenderCondition.DOOR_LEFT_OPEN;
                                            break;
                                        case DOOR_LEFT_CLOSED:
                                            newRenderCondition = ResourcePackCreatorProperties.RenderCondition.DOOR_RIGHT_CLOSED;
                                            break;
                                        case DOOR_RIGHT_CLOSED:
                                            newRenderCondition = ResourcePackCreatorProperties.RenderCondition.DOOR_LEFT_CLOSED;
                                            break;
                                    }
                                    newPartObj.remove("render_condition");
                                    newPartObj.addProperty("render_condition", newRenderCondition.toString());
                                    // Reverse position offsets
                                    JsonArray newPositions = new JsonArray();
                                    JsonArray oldPositions = newPartObj.get("positions").getAsJsonArray();
                                    for (int j = 0; j < oldPositions.size(); j++) {
                                        JsonArray pos = oldPositions.get(j).getAsJsonArray();
                                        pos.set(1, new JsonPrimitive(-pos.get(1).getAsFloat()));
                                        newPositions.add(pos);
                                    }
                                    newPartObj.remove("positions");
                                    newPartObj.add("positions", newPositions);
                                }
                                newParts.add(newPartObj);
                            }
                        }
                        target.properties.remove(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS);
                        target.properties.add(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS, newParts);
                    } else {
                        models = ObjModelLoader.loadModels(
                                MtrModelRegistryUtil.resourceManager,
                                new ResourceLocation(modelLocations),
                                MainClient.atlasManager
                        );
                    }
                } else {
                    String path = MtrModelRegistryUtil.getPathFromDummyBbData(model);
                    if (cachedModels == null
                            || !path.equals(cachedPath) || new File(path).lastModified() != cachedPathMtime) {
                        MainClient.modelManager.clearNamespace("mtrsteamloco-external");
                        cachedModels = ObjModelLoader.loadExternalModels(
                                MtrModelRegistryUtil.getPathFromDummyBbData(model),
                                MainClient.atlasManager
                        );
                        // Apply logo texture to make it look more interesting
                        for (RawModel partModel : cachedModels.values()) {
                            partModel.replaceAllTexture(MtrModelRegistryUtil.PLACEHOLDER_TILE_TEXTURE_LOCATION);
                        }
                        cachedPath = path;
                        cachedPathMtime = new File(path).lastModified();
                    }
                    models = cachedModels;
                }

                // Apply repaint texture
                String repaintTexture = MtrModelRegistryUtil.getTextureIdFromDummyBbData(model);
                if (!StringUtils.isEmpty(repaintTexture)) {
                    for (RawModel partModel : models.values()) {
                        partModel.replaceAllTexture("default.png", new ResourceLocation(repaintTexture));
                    }
                }
                // Apply FlipV
                if (MtrModelRegistryUtil.getFlipVFromDummyBbData(model)) {
                    for (RawModel partModel : models.values()) {
                        partModel.applyUVMirror(false, true);
                    }
                }

                JsonArray propertyParts = target.properties.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS);
                Map<String, RawModel> finalModels = models;
                propertyParts.forEach(jsonElement -> {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final String name = jsonObject.get("name").getAsString();
                    RawModel partModel = finalModels.getOrDefault(name, null);

                    if (partModel != null) {
                        final ModelTrainBase.RenderStage renderStage = EnumHelper.valueOf(ModelTrainBase.RenderStage.EXTERIOR,
                                jsonObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_STAGE).getAsString().toUpperCase(Locale.ROOT));
                        switch (renderStage) {
                            case EXTERIOR:
                                partModel.setAllRenderType("reset");
                                break;
                            case INTERIOR:
                                partModel.setAllRenderType("interior");
                                break;
                            case INTERIOR_TRANSLUCENT:
                                partModel.setAllRenderType("interiortranslucent");
                                break;
                            case LIGHTS:
                            case ALWAYS_ON_LIGHTS:
                                partModel.setAllRenderType("light");
                                break;
                        }
                    }
                });

                boolean isLoadingFromEditor = !GlStateTracker.isStateProtected;
                if (isLoadingFromEditor) GlStateTracker.capture();
                for (Map.Entry<String, RawModel> entry : models.entrySet()) {
                    target.parts.put(entry.getKey(), new SowcerModelAgent(entry.getValue()));
                }
                if (isLoadingFromEditor) GlStateTracker.restore();
            } catch (Exception e) {
                Main.LOGGER.error("Failed loading OBJ into DynamicTrainModel", e);
                MtrModelRegistryUtil.recordLoadingError("OBJ model " + MtrModelRegistryUtil.getPathFromDummyBbData(model), e);
            }
        }
    }
}
