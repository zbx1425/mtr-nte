package cn.zbx1425.mtrsteamloco.mixin;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Variant.Deserializer.class)
public class VariantDeserializerMixin {

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
    void getModel(JsonObject json, CallbackInfoReturnable<ResourceLocation> cir) {
        final String[] interestedPaths = {
                "block/rail_connection", "block/rail_connection_22_5",
                "block/rail_connection_45", "block/rail_connection_67_5"
        };
        ResourceLocation rl = cir.getReturnValue();
        if (rl.getNamespace().equals("mtr") && StringUtils.equalsAny(rl.getPath(), interestedPaths)) {
            cir.setReturnValue(new ResourceLocation("mtrsteamloco:block/null"));
        }
    }
}
