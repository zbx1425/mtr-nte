package cn.zbx1425.mtrsteamloco.render.integration;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.CustomResources;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.mixin.ModelMapperAccessor;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.client.DynamicTrainModel;
import mtr.client.DynamicTrainModelLegacy;
import mtr.client.IResourcePackCreatorProperties;
import mtr.client.ResourcePackCreatorProperties;
import mtr.data.EnumHelper;
import mtr.mappings.ModelMapper;
import mtr.mappings.UtilitiesClient;
import mtr.model.ModelTrainBase;
import mtr.render.RenderTrains;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class DynamicTrainModelLoader {

    private static Map<String, RawModel> cachedModels;
    private static String cachedPath;
    private static long cachedPathMtime = 0;

    public static void loadInto(JsonObject model, DynamicTrainModel target) {
        if (MtrModelRegistryUtil.isDummyBbData(model)) {
            loadObjInto(model, target);
        } else {
            if (!model.has("dummyBbData")) return;
            boolean bbModelPreload = MtrModelRegistryUtil.getBbModelPreloadFromDummyBbData(
                    model.get("dummyBbData").getAsJsonObject());
            if (ClientConfig.enableBbModelPreload || bbModelPreload) {
                loadVanillaModelInto(model, target);
            }
        }
    }

    public static void loadObjInto(JsonObject model, DynamicTrainModel target) {
        int bbDataType = MtrModelRegistryUtil.getDummyBbDataType(model);
        String path = MtrModelRegistryUtil.getPathFromDummyBbData(model);
        target.parts.clear();
        try {
            if (target.properties.has("atlasIndex")) {
                MainClient.atlasManager.load(
                        MtrModelRegistryUtil.resourceManager,
                        new ResourceLocation(target.properties.get("atlasIndex").getAsString())
                );
            }

            Map<String, RawModel> models;
            if (bbDataType == 1) {
                String modelLocations = MtrModelRegistryUtil.getPathFromDummyBbData(model);
                if (modelLocations.contains("|")) {
                    models = new HashMap<>();
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
                                newDoorOffset = switch (newDoorOffset) {
                                    case LEFT_POSITIVE -> ResourcePackCreatorProperties.DoorOffset.RIGHT_NEGATIVE;
                                    case RIGHT_POSITIVE -> ResourcePackCreatorProperties.DoorOffset.LEFT_NEGATIVE;
                                    case LEFT_NEGATIVE -> ResourcePackCreatorProperties.DoorOffset.RIGHT_POSITIVE;
                                    case RIGHT_NEGATIVE -> ResourcePackCreatorProperties.DoorOffset.LEFT_POSITIVE;
                                    default -> newDoorOffset;
                                };
                                newPartObj.remove("door_offset");
                                newPartObj.addProperty("door_offset", newDoorOffset.toString());
                                // Reverse render conditions
                                ResourcePackCreatorProperties.RenderCondition newRenderCondition =
                                        EnumHelper.valueOf(ResourcePackCreatorProperties.RenderCondition.ALL, newPartObj.get("render_condition").getAsString().toUpperCase(Locale.ROOT));
                                newRenderCondition = switch (newRenderCondition) {
                                    case DOOR_LEFT_OPEN -> ResourcePackCreatorProperties.RenderCondition.DOOR_RIGHT_OPEN;
                                    case DOOR_RIGHT_OPEN -> ResourcePackCreatorProperties.RenderCondition.DOOR_LEFT_OPEN;
                                    case DOOR_LEFT_CLOSED -> ResourcePackCreatorProperties.RenderCondition.DOOR_RIGHT_CLOSED;
                                    case DOOR_RIGHT_CLOSED -> ResourcePackCreatorProperties.RenderCondition.DOOR_LEFT_CLOSED;
                                    default -> newRenderCondition;
                                };
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
                    partModel.replaceTexture("default.png", new ResourceLocation(repaintTexture));
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
                            partModel.setAllRenderType("light");
                            break;
                        case ALWAYS_ON_LIGHTS:
                            partModel.setAllRenderType("lighttranslucent");
                            break;
                    }
                }
            });

            if (bbDataType == 1) {
                // Loading from a resource pack. Merge the parts.
                Map<PartBatch, RawModel> mergedModels = new HashMap<>();
                target.properties.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS).forEach(elem -> {
                    JsonObject partObject = elem.getAsJsonObject();
                    String partName = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_NAME).getAsString();
                    RawModel partModel = models.get(partName);
                    if (partModel == null) return;

                    final boolean mirror = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_MIRROR).getAsBoolean();
                    PartBatch batch = new PartBatch(partObject, mirror);
                    RawModel mergedModel = mergedModels.computeIfAbsent(batch, ignored -> new RawModel());

                    partObject.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_POSITIONS).forEach(positionElement -> {
                        final float x = positionElement.getAsJsonArray().get(0).getAsFloat() / 16f;
                        final float z = positionElement.getAsJsonArray().get(1).getAsFloat() / 16f;
                        if (!mirror && x == 0 && z == 0) {
                            mergedModel.append(partModel);
                        } else {
                            RawModel clonedModel = partModel.copy();
                            if (mirror) {
                                clonedModel.applyRotation(Vector3f.YP, 180);
                                clonedModel.applyTranslation(-x, 0, z);
                            } else {
                                clonedModel.applyTranslation(x, 0, z);
                            }
                            mergedModel.append(clonedModel);
                        }
                    });
                });

                target.parts.clear();
                JsonArray partsPropArray = new JsonArray();
                target.properties.add(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS, partsPropArray);
                boolean isLoadingFromEditor = !GlStateTracker.isStateProtected;
                if (isLoadingFromEditor) GlStateTracker.capture();
                for (Map.Entry<PartBatch, RawModel> entry : mergedModels.entrySet()) {
                    PartBatch batch = entry.getKey();
                    RawModel mergedModel = entry.getValue();
                    target.parts.put(batch.batchId, new SowcerModelAgent(mergedModel, false));
                    partsPropArray.add(batch.getPartObject());
                }
                if (isLoadingFromEditor) GlStateTracker.restore();
            } else {
                // Loading in RP editor. Don't merge the parts.
                boolean isLoadingFromEditor = !GlStateTracker.isStateProtected;
                if (isLoadingFromEditor) GlStateTracker.capture();
                for (Map.Entry<String, RawModel> entry : models.entrySet()) {
                    target.parts.put(entry.getKey(), new SowcerModelAgent(entry.getValue(), false));
                }
                if (isLoadingFromEditor) GlStateTracker.restore();
            }
        } catch (Exception e) {
            Main.LOGGER.error("Failed loading OBJ into DynamicTrainModel", e);
            MtrModelRegistryUtil.recordLoadingError("Failed loading OBJ model " + path, e);
        }
    }

    public static void loadVanillaModelInto(JsonObject model, DynamicTrainModel target) {
        if (!model.has("dummyBbData")) return;
        String path = MtrModelRegistryUtil.getPathFromDummyBbData(model.get("dummyBbData").getAsJsonObject());
        try {
            String textureId = MtrModelRegistryUtil.getTextureIdFromDummyBbData(model.get("dummyBbData").getAsJsonObject());
            ResourceLocation texture = resolveTexture(textureId, str -> str.endsWith(".png") ? str : (str + ".png"));

            Map<PartBatch, CapturingVertexConsumer> mergeVertexConsumers = new HashMap<>();
            target.properties.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS).forEach(elem -> {
                JsonObject partObject = elem.getAsJsonObject();
                String partName = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_NAME).getAsString();
                ModelMapper partModel = target.parts.get(partName);
                if (partModel == null) return;

                ModelTrainBase.RenderStage renderStage = EnumHelper.valueOf(ModelTrainBase.RenderStage.EXTERIOR,
                        partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_STAGE).getAsString().toUpperCase(Locale.ROOT));
                final boolean mirror = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_MIRROR).getAsBoolean();
                PartBatch batch = new PartBatch(partObject, mirror);
                CapturingVertexConsumer vertexConsumer = mergeVertexConsumers.computeIfAbsent(batch, ignored -> new CapturingVertexConsumer());

                vertexConsumer.beginStage(texture, renderStage);
                partObject.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_POSITIONS).forEach(positionElement -> {
                    final float x = positionElement.getAsJsonArray().get(0).getAsFloat();
                    final float z = positionElement.getAsJsonArray().get(1).getAsFloat();
                    ModelPart modelPart = ((ModelMapperAccessor)partModel).getModelPart();
                    if (mirror) {
                        modelPart.setPos(-x, 0, z);
                        modelPart.yRot = (float) Math.PI;
                    } else {
                        modelPart.setPos(x, 0, z);
                        modelPart.yRot = 0;
                    }
                    vertexConsumer.captureModelPart(modelPart);
                });
            });

            target.parts.clear();
            JsonArray partsPropArray = new JsonArray();
            List<JsonObject> propertiesToKeep = new ArrayList<>();
            target.properties.getAsJsonArray(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS).forEach(partElement -> {
                final JsonObject partObject = partElement.getAsJsonObject();
                final String partName = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_NAME).getAsString();
                if (!target.partsInfo.containsKey(partName) || !partObject.has(IResourcePackCreatorProperties.KEY_PROPERTIES_DISPLAY)) {
                    return;
                }
                propertiesToKeep.add(partObject);
            });
            target.properties.add(IResourcePackCreatorProperties.KEY_PROPERTIES_PARTS, partsPropArray);
            for (JsonObject partObject : propertiesToKeep) {
                partsPropArray.add(partObject);
            }

            for (Map.Entry<PartBatch, CapturingVertexConsumer> entry : mergeVertexConsumers.entrySet()) {
                PartBatch batch = entry.getKey();
                CapturingVertexConsumer vertexConsumer = entry.getValue();
                RawModel rawModel = vertexConsumer.models[0];
                rawModel.triangulate();
                target.parts.put(batch.batchId, new SowcerModelAgent(rawModel, true));
                partsPropArray.add(batch.getPartObject());
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error when optimizing BBMODEL in DynamicTrainModel", e);
            MtrModelRegistryUtil.recordLoadingError("Error when optimizing BBMODEL " + path, e);
        }
    }

    public static class PartBatch {

        public final ResourcePackCreatorProperties.DoorOffset doorOffset;
        public final ResourcePackCreatorProperties.RenderCondition renderCondition;
        public final String whitelistedCars;
        public final String blacklistedCars;

        public final boolean skipRenderingIfTooFar;

        public final String batchId;

        public PartBatch(JsonObject partObject, boolean mirror) {
            ResourcePackCreatorProperties.DoorOffset rawDoorOffset = EnumHelper.valueOf(ResourcePackCreatorProperties.DoorOffset.NONE,
                    partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_DOOR_OFFSET).getAsString());
            if (mirror) {
                // Compensate for the lost mirror property in combined part
                doorOffset = switch (rawDoorOffset) {
                    case NONE -> ResourcePackCreatorProperties.DoorOffset.NONE;
                    case LEFT_NEGATIVE -> ResourcePackCreatorProperties.DoorOffset.LEFT_POSITIVE;
                    case LEFT_POSITIVE -> ResourcePackCreatorProperties.DoorOffset.LEFT_NEGATIVE;
                    case RIGHT_NEGATIVE -> ResourcePackCreatorProperties.DoorOffset.RIGHT_POSITIVE;
                    case RIGHT_POSITIVE -> ResourcePackCreatorProperties.DoorOffset.RIGHT_NEGATIVE;
                };
            } else {
                doorOffset = rawDoorOffset;
            }
            this.renderCondition = EnumHelper.valueOf(ResourcePackCreatorProperties.RenderCondition.ALL,
                    partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_RENDER_CONDITION).getAsString());
            this.whitelistedCars = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_WHITELISTED_CARS).getAsString();
            this.blacklistedCars = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_BLACKLISTED_CARS).getAsString();

            final ModelTrainBase.RenderStage renderStage = EnumHelper.valueOf(ModelTrainBase.RenderStage.EXTERIOR,
                    partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_STAGE).getAsString().toUpperCase(Locale.ROOT));
            this.skipRenderingIfTooFar = partObject.get(IResourcePackCreatorProperties.KEY_PROPERTIES_SKIP_RENDERING_IF_TOO_FAR).getAsBoolean()
                || renderStage == ModelTrainBase.RenderStage.INTERIOR_TRANSLUCENT;

            this.batchId = String.format("$NTEPart:%s:%s:%s:%s:%s", doorOffset, renderCondition, whitelistedCars, blacklistedCars, skipRenderingIfTooFar);
        }

        public JsonObject getPartObject() {
            JsonObject result = new JsonObject();
            result.addProperty("name", batchId);
            result.addProperty("stage", "EXTERIOR");
            result.addProperty("mirror", false);
            result.addProperty("skip_rendering_if_too_far", skipRenderingIfTooFar);
            result.addProperty("door_offset", doorOffset.toString());
            result.addProperty("render_condition", renderCondition.toString());
            result.add("positions", new JsonParser().parse("[[0, 0]]"));
            result.addProperty("whitelisted_cars", whitelistedCars);
            result.addProperty("blacklisted_cars", blacklistedCars);
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartBatch partBatch = (PartBatch) o;
            return batchId.equals(partBatch.batchId);
        }

        @Override
        public int hashCode() {
            return batchId.hashCode();
        }
    }

    private static ResourceLocation resolveTexture(String textureId, Function<String, String> formatter) {
        final String textureString = formatter.apply(textureId);
        final ResourceLocation id = new ResourceLocation(textureString);
        final boolean available;

        if (!RenderTrains.AVAILABLE_TEXTURES.contains(textureString) && !RenderTrains.UNAVAILABLE_TEXTURES.contains(textureString)) {
            available = UtilitiesClient.hasResource(id);
            (available ? RenderTrains.AVAILABLE_TEXTURES : RenderTrains.UNAVAILABLE_TEXTURES).add(textureString);
            if (!available) {
                System.out.println("Texture " + textureString + " not found, using default");
            }
        } else {
            available = RenderTrains.AVAILABLE_TEXTURES.contains(textureString);
        }

        if (available) {
            return id;
        } else {
            return new ResourceLocation("mtr:textures/block/transparent.png");
        }
    }
}
