package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.render.integration.DynamicTrainModelLoader;
import com.google.gson.JsonObject;
import mtr.client.DoorAnimationType;
import mtr.client.DynamicTrainModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(DynamicTrainModel.class)
public class DynamicTrainModelMixin {

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void ctor(JsonObject model, JsonObject properties, DoorAnimationType doorAnimationType, CallbackInfo ci) {
        DynamicTrainModelLoader.loadInto(model, (DynamicTrainModel)(Object)this);
    }
}
