package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.train.NoopTrainRenderer;
import cn.zbx1425.mtrsteamloco.sound.NoopTrainSound;
import mtr.data.TrainClient;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrainClient.class)
public class TrainClientMixin {

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void initTail(FriendlyByteBuf packet, CallbackInfo ci) {
        if (!ClientConfig.enableTrainRender) ((TrainClientAccessor)this).setTrainRenderer(NoopTrainRenderer.INSTANCE);
        if (!ClientConfig.enableTrainSound) ((TrainClientAccessor)this).setTrainSound(NoopTrainSound.INSTANCE);
    }
}
