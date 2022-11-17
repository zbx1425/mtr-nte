package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.CustomResources;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.function.Consumer;

@Mixin(mtr.client.CustomResources.class)
public abstract class CustomResourcesMixin {

    @Shadow
    private static void readResource(ResourceManager manager, String path, Consumer<JsonObject> callback) { }

    @Inject(at = @At("HEAD"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private static void reloadHead(ResourceManager manager, CallbackInfo ci) {
        MtrModelRegistryUtil.resourceManager = manager;
    }

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private static void reloadTail(ResourceManager manager, CallbackInfo ci) {
        CustomResources.init(manager);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lmtr/client/CustomResources;readResource(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/lang/String;Ljava/util/function/Consumer;)V"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private static void redirectReadResource(ResourceManager manager, String path, Consumer<JsonObject> callback) {
        if (path.toLowerCase(Locale.ROOT).endsWith(".obj")) {
            callback.accept(MtrModelRegistryUtil.createDummyBbData(new ResourceLocation(path)));
        } else {
            readResource(manager, path, callback);
        }
        CustomResources.init(manager);
    }
}
