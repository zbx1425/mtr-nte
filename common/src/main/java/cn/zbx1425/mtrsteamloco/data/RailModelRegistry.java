package cn.zbx1425.mtrsteamloco.data;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawMesh;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.Vertex;
import mtr.mappings.Text;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RailModelRegistry {

    public static Map<String, RailModelProperties> elements = new HashMap<>();

    public static void register(String key, Component name, RawModel rawModel, float repeatInterval) {
        elements.put(key, new RailModelProperties(name, rawModel, repeatInterval));
    }

    public static void reload(ResourceManager resourceManager) {
        for (RailModelProperties element : elements.values()) {
            element.close();
        }
        elements.clear();

        try {
            // This is hardcoded in BakedRail to never be pulled from registry
            register("", Text.translatable("rail.mtrsteamloco.default"), null, 1000000f);
            // This is pulled from registry and shouldn't be shown
            register("null", Text.translatable("rail.mtrsteamloco.hidden"), null, 1000000f);

            RawModel rawCommonRailModel = MainClient.modelManager.loadRawModel(
                    resourceManager, new ResourceLocation("mtrsteamloco:models/rail.obj"), MainClient.atlasManager);
            register("nte_builtin_concrete_sleeper", Text.translatable("rail.mtrsteamloco.builtin_concrete_sleeper"), rawCommonRailModel, 0.5f);

            RawModel rawSidingRailModel = MainClient.modelManager.loadRawModel(
                    resourceManager, new ResourceLocation("mtrsteamloco:models/rail_siding.obj"), MainClient.atlasManager);
            register("nte_builtin_depot", Text.translatable("rail.mtrsteamloco.builtin_depot"), rawSidingRailModel, 0.5f);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MainClient.railRenderDispatcher.clearRail();
    }

    public static RawModel getRawModel(String key) {
        return elements.containsKey(key) ? elements.get(key).rawModel : null;
    }

    public static Model getUploadedModel(String key) {
        return elements.containsKey(key) ? elements.get(key).uploadedModel : null;
    }

    public static Long getBoundingBox(String key) {
        return elements.containsKey(key) ? elements.get(key).boundingBox : 0;
    }

    public static float getRepeatInterval(String key) {
        return elements.containsKey(key) ? elements.get(key).repeatInterval : 0.5f;
    }
}
