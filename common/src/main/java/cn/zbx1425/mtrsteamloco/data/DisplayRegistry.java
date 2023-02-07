package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplaySink;
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

    public static Map<String, DisplaySink> trainSinks = new HashMap<>();

    public static void reload(ResourceManager resourceManager) {
        for (DisplaySink sink : trainSinks.values()) {
            sink.close();
        }
        trainSlots.clear();
        trainSinks.clear();

        readResource(resourceManager, "mtr:" + ICustomResources.CUSTOM_RESOURCES_ID + ".json", jsonObject -> {
            jsonObject.get(ICustomResources.CUSTOM_TRAINS_KEY).getAsJsonObject().entrySet().forEach(entry -> {
                try {
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
                        DisplaySink sink = new DisplaySink(resourceManager, sinkLocation,
                                Main.JSON_PARSER.parse(ResourceUtil.readResource(resourceManager, sinkLocation)).getAsJsonObject(),
                                slots);

                        trainSlots.put(entry.getKey(), slots);
                        trainSinks.put(entry.getKey(), sink);
                    }
                } catch (Exception ex) {
                    Main.LOGGER.error("Failed loading train display: " + entry.getKey(), ex);
                    MtrModelRegistryUtil.loadingErrorList.add("Train Display " + entry.getKey()
                            + ExceptionUtils.getStackTrace(ex));
                }
            });
        });
    }

    public static void handleDraw(String trainId, TrainClient train, int ridingCar,
                                  double x, double y, double z, float yaw, float pitch,
                                  boolean doorLeftOpen, boolean doorRightOpen) {
        if (trainSinks.containsKey(trainId)) {
            if (RenderUtil.shouldSkipRenderTrain(train)) return;
            PoseStack matrices = RenderUtil.commonPoseStack;
            final BlockPos posAverage = TrainRendererBase.applyAverageTransform(train.getViewOffset(), x, y, z);
            if (posAverage == null) {
                return;
            }

            matrices.translate(x, y - 1, z);
            PoseStackUtil.rotY(matrices, (float) Math.PI + yaw);
            final boolean hasPitch = pitch < 0 ? train.transportMode.hasPitchAscending : train.transportMode.hasPitchDescending;
            PoseStackUtil.rotX(matrices, hasPitch ? pitch : 0);

            Matrix4f pose = new Matrix4f(matrices.last().pose());
            trainSinks.get(trainId).update(train, pose, ridingCar, doorLeftOpen, doorRightOpen);
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
