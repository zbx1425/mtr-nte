package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {

    @Inject(at = @At("TAIL"), method = "handleLightUpdatePacket")
    private void onLightPacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
        Minecraft.getInstance().execute(() -> {
            MainClient.railRenderDispatcher.registerLightUpdate(packet.getX(), packet.getZ());
        });
    }
}
