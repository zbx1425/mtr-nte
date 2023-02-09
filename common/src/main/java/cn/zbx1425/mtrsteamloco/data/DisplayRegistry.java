package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.display.DisplaySlot;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.PoseStackUtil;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.client.ICustomResources;
import mtr.data.TrainClient;
import mtr.mappings.Utilities;
import mtr.mappings.UtilitiesClient;
import mtr.render.TrainRendererBase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DisplayRegistry {

    public static Map<String, Map<String, DisplaySlot>> trainSlots = new HashMap<>();

    public static Map<String, DisplayContent> trainSinks = new HashMap<>();

    public static void reload(ResourceManager resourceManager) {
        for (DisplayContent content : trainSinks.values()) {
            content.close();
        }
        trainSlots.clear();
        trainSinks.clear();

        readResource(resourceManager, "mtr:" + ICustomResources.CUSTOM_RESOURCES_ID + ".json", jsonObject -> {
            jsonObject.get(ICustomResources.CUSTOM_TRAINS_KEY).getAsJsonObject().entrySet().forEach(entry -> {
                try {
                    String trainId = ICustomResources.CUSTOM_TRAIN_ID_PREFIX + entry.getKey();
                    JsonObject trainObj = entry.getValue().getAsJsonObject();
                    if (trainObj.has("display_slots") && trainObj.has("display_content")) {
                        Map<String, DisplaySlot> slots = new HashMap<>();
                        JsonArray slotArray = Main.JSON_PARSER.parse(ResourceUtil.readResource(resourceManager,
                                new ResourceLocation(trainObj.get("display_slots").getAsString())))
                                .getAsJsonObject().get("slots").getAsJsonArray();
                        for (int i = 0; i < slotArray.size(); i++) {
                            DisplaySlot slot = new DisplaySlot(slotArray.get(i).getAsJsonObject());
                            slots.put(slot.name, slot);
                        }

                        ResourceLocation sinkLocation = new ResourceLocation(trainObj.get("display_content").getAsString());
                        DisplayContent content = new DisplayContent(resourceManager, sinkLocation,
                                Main.JSON_PARSER.parse(ResourceUtil.readResource(resourceManager, sinkLocation)).getAsJsonObject(),
                                slots);

                        trainSlots.put(trainId, slots);
                        trainSinks.put(trainId, content);
                    }
                } catch (Exception ex) {
                    Main.LOGGER.error("Failed loading train display: " + entry.getKey(), ex);
                    MtrModelRegistryUtil.loadingErrorList.add("Train Display " + entry.getKey()
                            + ExceptionUtils.getStackTrace(ex));
                }
            });
        });
    }

    public static void drawAllImmediate() {
        for (DisplayContent content : trainSinks.values()) {
            content.drawImmediate();
        }
    }

    public static void handleCar(String trainId, TrainClient train, int ridingCar,
                                 double carX, double carY, double carZ, float yaw, float pitch,
                                 boolean doorLeftOpen, boolean doorRightOpen) {
        if (trainSinks.containsKey(trainId)) {
            if (RenderUtil.shouldSkipRenderTrain(train)) return;

            PoseStack matrices = RenderUtil.commonPoseStack;
            Vec3 offset = train.vehicleRidingClient.getVehicleOffset();
            if (offset == null) offset = Vec3.ZERO;
            double newX = carX - offset.x;
            double newY = carY - offset.y;
            double newZ = carZ - offset.z;
            final BlockPos posAverage = TrainRendererBase.applyAverageTransform(train.getViewOffset(), newX, newY, newZ);
            if (posAverage == null) {
                return;
            }

            matrices.translate(newX, newY, newZ);
            PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
            final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
            PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);

            Matrix4f pose = new Matrix4f(matrices.last().pose());
            try {
                trainSinks.get(trainId).handleCar(train, pose, ridingCar, doorLeftOpen, doorRightOpen);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            matrices.popPose();
        }
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

}
