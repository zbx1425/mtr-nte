package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {

#if MC_VERSION >= "11800"
    @Inject(at = @At("TAIL"), method = "handleLightUpdatePacket")
#else
    @Inject(at = @At("TAIL"), method = "handleLightUpdatePacked")
#endif
    private void onLightPacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
        BlockPos refBp = new BlockPos(packet.getX(), 51, 10);
        RenderUtil.displayStatusMessage("NET CALLED! " + refBp.toString());
        RenderUtil.queueFrameEndTask(() -> {
            if (packet.getZ() == 0) {
                if (Minecraft.getInstance().level.getBrightness(LightLayer.BLOCK, refBp) == 0) {
                    RenderUtil.displayStatusMessage(String.format("NET LIGHT is 0! : %-3d", packet.getX()));
                } else {
                    RenderUtil.displayStatusMessage(String.format("NET OK! : %-3d", packet.getX()));
                }
            }
            MainClient.railRenderDispatcher.registerLightUpdate(packet.getX(), packet.getZ());
        });
    }
}
