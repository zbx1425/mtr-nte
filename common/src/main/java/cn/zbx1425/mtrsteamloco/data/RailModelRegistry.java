package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RailModelRegistry {

    public static Map<String, RawModel> rawElements = new HashMap<>();
    public static Map<String, Model> uploadedElements = new HashMap<>();

    public static void register(String key, RawModel rawModel) {
        rawModel.clearAttrStates();
        rawModel.applyRotation(new Vector3f(0.577f, 0.577f, 0.577f), (float)Math.toRadians(2));
        rawElements.put(key, rawModel);
        uploadedElements.put(key, MainClient.modelManager.uploadModel(rawModel));
    }

    public static void reload(ResourceManager resourceManager) {
        rawElements.clear();
        uploadedElements.clear();

        try {
            RawModel rawCommonRailModel = MainClient.modelManager.loadRawModel(
                    resourceManager, new ResourceLocation("mtrsteamloco:models/rail.obj"), MainClient.atlasManager);
            register("rail", rawCommonRailModel);

            RawModel rawSidingRailModel = MainClient.modelManager.loadRawModel(
                    resourceManager, new ResourceLocation("mtrsteamloco:models/rail_siding.obj"), MainClient.atlasManager);
            register("rail_siding", rawSidingRailModel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MainClient.railRenderDispatcher.clearRail();
    }

    public static RawModel getRawModel(String key) {
        return rawElements.getOrDefault(key, null);
    }

    public static Model getUploadedModel(String key) {
        return uploadedElements.getOrDefault(key, null);
    }
}
