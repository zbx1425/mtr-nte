package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.CustomResources;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.gui.ErrorScreen;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.mtrsteamloco.render.scripting.train.ScriptedTrainRenderer;
import cn.zbx1425.sowcer.util.GlStateTracker;
import mtr.client.TrainClientRegistry;
import mtr.data.TransportMode;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
    private void handleDebugKeysHead(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == GLFW.GLFW_KEY_5 && ClientConfig.enableScriptDebugOverlay) {
            minecraft.tell(() -> {
                GlStateTracker.capture();
                MtrModelRegistryUtil.loadingErrorList.clear();
                MtrModelRegistryUtil.resourceManager = minecraft.getResourceManager();
                ScriptHolder.resetRunner();

                for (TransportMode transportMode : TransportMode.values()) {
                    TrainClientRegistry.forEach(transportMode, (id, prop) -> {
                        if (prop.renderer instanceof ScriptedTrainRenderer) {
                            try {
                                ((ScriptedTrainRenderer) prop.renderer).typeScripting.reload(minecraft.getResourceManager());
                            } catch (Exception ex) {
                                MtrModelRegistryUtil.recordLoadingError("Failed to reload train script: " + id, ex);
                            }
                        }
                    });
                }
                for (Map.Entry<String, EyeCandyProperties> entry : EyeCandyRegistry.elements.entrySet()) {
                    if (entry.getValue().script != null) {
                        try {
                            entry.getValue().script.reload(minecraft.getResourceManager());
                        } catch (Exception ex) {
                            MtrModelRegistryUtil.recordLoadingError("Failed to reload eye-candy script: " + entry.getKey(), ex);
                        }
                    }
                }

                CustomResources.resetComponents();


                GlStateTracker.restore();
                if (!MtrModelRegistryUtil.loadingErrorList.isEmpty()) {
                    minecraft.execute(() -> {
                        minecraft.setScreen(ErrorScreen.createScreen(MtrModelRegistryUtil.loadingErrorList, minecraft.screen));
                    });
                }
            });
            cir.setReturnValue(true);
        }
    }
}
