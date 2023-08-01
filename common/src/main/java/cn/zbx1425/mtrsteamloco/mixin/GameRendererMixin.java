package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.network.Patreon;
import cn.zbx1425.mtrsteamloco.render.rail.RailRenderDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /*
    @ModifyVariable(method = "getProjectionMatrix", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    double getProjectionMatrixHead(double value) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof SelectButtonsScreen && ((SelectButtonsScreen)currentScreen).isSelecting()) {
            return 100;
        } else {
            return value;
        }
    }
    */

    @Shadow @Final private Minecraft minecraft;
#if !NO_SPONSOR_TEST
    @Shadow @Final private static Logger LOGGER;
#endif
    private Boolean hideGuiOptionCache = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    void initTail(Minecraft minecraft, ItemInHandRenderer itemInHandRenderer, ResourceManager resourceManager, RenderBuffers renderBuffers, CallbackInfo ci) {
#if !NO_SPONSOR_TEST
        ArrayList<Patreon> patreonList = new ArrayList<>();
        Patreon.getPatreonList(patreonList);
        // TODO display sponsors
        LOGGER.info(Patreon.PATREON_API_KEY.split("-")[1]);
#endif
    }

#if MC_VERSION >= "11903"
    @Inject(method = "getProjectionMatrix", at = @At("TAIL"), cancellable = true)
    void getProjectionMatrixTail(double fov, CallbackInfoReturnable<org.joml.Matrix4f> cir) {
        if (RailRenderDispatcher.isPreviewingModel) {
            org.joml.Matrix4f result = new org.joml.Matrix4f();
            result.translation(0.5f, 0f, 0f);
            result.scale(0.8f, 0.8f, 1f);
            result.mul(cir.getReturnValue());
#else
    @Inject(method = "getProjectionMatrix", at = @At("TAIL"), cancellable = true)
    void getProjectionMatrixTail(double fov, CallbackInfoReturnable<com.mojang.math.Matrix4f> cir) {
        if (RailRenderDispatcher.isPreviewingModel) {
            com.mojang.math.Matrix4f result = com.mojang.math.Matrix4f.createTranslateMatrix(0.5f, 0f, 0f);
            result.multiply(com.mojang.math.Matrix4f.createScaleMatrix(0.8f, 0.8f, 1f));
            result.multiply(cir.getReturnValue());
#endif

            cir.setReturnValue(result);
        }
        if (RailRenderDispatcher.isPreviewingModel && hideGuiOptionCache == null) {
            hideGuiOptionCache = minecraft.options.hideGui;
            minecraft.options.hideGui = true;
        } else if (!RailRenderDispatcher.isPreviewingModel && hideGuiOptionCache != null) {
            minecraft.options.hideGui = hideGuiOptionCache;
            hideGuiOptionCache = null;
        }
    }
}
