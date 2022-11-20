package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.CustomResources;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mtr.client.ICustomResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(mtr.client.CustomResources.class)
public class CustomResourcesMixin {

    @Inject(at = @At("HEAD"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private static void reloadHead(ResourceManager manager, CallbackInfo ci) {
        MtrModelRegistryUtil.resourceManager = manager;
        CustomResources.reset(manager);
    }

    @Inject(at = @At("TAIL"), method = "reload(Lnet/minecraft/server/packs/resources/ResourceManager;)V")
    private static void reloadTail(ResourceManager manager, CallbackInfo ci) {
        CustomResources.init(manager);
    }

    @Inject(at = @At("HEAD"), method = "readResource", cancellable = true)
    private static void readResource(ResourceManager manager, String path, Consumer<JsonObject> callback, CallbackInfo ci) {
        if (path.toLowerCase(Locale.ROOT).endsWith(".obj") || path.contains("|")) {
            JsonObject dummyBbData = MtrModelRegistryUtil.createDummyBbDataWithRl(path);
            dummyBbData.addProperty("textureId", capturedTextureId);
            callback.accept(dummyBbData);
            ci.cancel();
        }
    }

    private static String capturedTextureId = "";

    @Inject(at = @At("RETURN"), method = "getOrDefault", remap = false)
    private static <T> void getOrDefault(JsonObject jsonObject, String key, T defaultValue, Function<JsonElement, T> function, CallbackInfoReturnable<T> cir) {
        if (key.equals(ICustomResources.CUSTOM_TRAINS_TEXTURE_ID)) {
            capturedTextureId = jsonObject.has(key) ? jsonObject.get(key).getAsString() : defaultValue.toString();
        }
    }

}
