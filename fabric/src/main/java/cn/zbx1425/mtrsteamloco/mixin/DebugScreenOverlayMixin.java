package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin {

    @Inject(method = "getGameInformation", at = @At("RETURN"))
    public void getGameInformation(CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();
        list.add("[SowCer] Draw Calls: " + MainClient.batchManager.drawCallCount + ", Batches: " + MainClient.batchManager.batchCount);
    }
}
