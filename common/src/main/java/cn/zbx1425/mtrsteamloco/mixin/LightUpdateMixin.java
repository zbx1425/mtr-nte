package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public class LightUpdateMixin {

    /**
     * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
     * lighting change occurred that tick. On the client, Minecraft uses this method to inform
     * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
     * and we should too.
     */
    @Inject(at = @At("HEAD"), method = "onLightUpdate")
    private void onLightUpdate(LightLayer layer, SectionPos pos, CallbackInfo ci) {
        ClientChunkCache thi = ((ClientChunkCache) (Object) this);
        ClientLevel world = (ClientLevel) thi.getLevel();

        if (world.equals(Minecraft.getInstance().level)) {
            MainClient.railRenderDispatcher.registerLightUpdate(pos.x(), pos.z());
        }
    }
}
