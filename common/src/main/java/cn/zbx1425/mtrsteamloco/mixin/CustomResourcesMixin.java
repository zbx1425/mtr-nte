package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.CustomResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(mtr.client.CustomResources.class)
public class CustomResourcesMixin {

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", remap = false)
    private static void reload(ResourceManager manager, CallbackInfo ci) {
        CustomResources.init(manager);
    }
}
