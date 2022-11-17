package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.integration.SowcerModelAgent;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.ObjModelLoader;
import com.google.gson.JsonObject;
import mtr.client.DoorAnimationType;
import mtr.client.DynamicTrainModel;
import mtr.client.IResourcePackCreatorProperties;
import mtr.mappings.ModelMapper;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.util.Map;

@Mixin(DynamicTrainModel.class)
public class DynamicTrainModelMixin {

    @Shadow @Final
    private Map<String, ModelMapper> parts;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ctor(JsonObject model, JsonObject properties, DoorAnimationType doorAnimationType, CallbackInfo ci) {
        if (MtrModelRegistryUtil.isDummyBbData(model)) {
            parts.clear();
            try {
                if (properties.has("atlasIndex")) {
                    MainClient.atlasManager.load(
                        MtrModelRegistryUtil.resourceManager,
                        new ResourceLocation(properties.get("atlasIndex").getAsString())
                    );
                }
                Map<String, RawModel> models = ObjModelLoader.loadModels(
                    MtrModelRegistryUtil.resourceManager,
                    MtrModelRegistryUtil.getRlFromDummyBbData(model),
                    MainClient.atlasManager
                );
                for (Map.Entry<String, RawModel> entry : models.entrySet()) {
                    parts.put(entry.getKey(), new SowcerModelAgent(entry.getValue()));
                }
            } catch (IOException e) {
                Main.LOGGER.error(e);
            }
        }
    }
}
