package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import mtr.client.DynamicTrainModel;
import mtr.client.ResourcePackCreatorProperties;
import mtr.mappings.ModelMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(ResourcePackCreatorProperties.class)
public class ResourcePackCreatorPropertiesMixin {

    @Inject(method = "readJson", at = @At("HEAD"), cancellable = true, remap = false)
    private static void readJson(Path path, BiConsumer<String, JsonObject> jsonCallback, CallbackInfo ci) {
        if (path.toString().toLowerCase(Locale.ROOT).endsWith(".obj")) {
            jsonCallback.accept(path.getFileName().toString(), MtrModelRegistryUtil.createDummyBbDataExternal(path.toString()));
            ci.cancel();
        }
    }

    @Shadow(remap = false)
    private JsonObject modelObject;

    @Shadow(remap = false)
    private DynamicTrainModel model;

    @Inject(method = "updateModel", at = @At("HEAD"), cancellable = true, remap = false)
    private void updateModelHead(CallbackInfo ci) {
        if (modelObject.size() == 0) {
            ci.cancel();
            return;
        }
        if (MtrModelRegistryUtil.getDummyBbDataType(modelObject) > 0) {
            modelObject.remove("outliner");
            modelObject.add("outliner", new JsonArray());
        }
    }

    @Inject(method = "updateModel", at = @At("TAIL"), remap = false)
    private void updateModelTail(CallbackInfo ci) {
        if (MtrModelRegistryUtil.getDummyBbDataType(modelObject) > 0) {
            JsonArray outlinerArray = new JsonArray();
            for (Map.Entry<String, ModelMapper> entry : model.parts.entrySet()) {
                JsonObject elementObject = new JsonObject();
                elementObject.addProperty("name", entry.getKey());
                outlinerArray.add(elementObject);
            }
            modelObject.remove("outliner");
            modelObject.add("outliner", outlinerArray);
        }
    }
}
