package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.MainClient;
import mtr.MTRClient;
import mtr.sound.bve.BveTrainSound;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BveTrainSound.class, remap = false)
public class BveTrainSoundMixin {

    @Shadow private int mrPress;
    @Shadow private boolean isCompressorActive;
    float newMrPress = 0;

    @Inject(method = "playNearestCar", at = @At("HEAD"))
    public void playNearestCar(Level world, BlockPos pos, int carIndex, CallbackInfo ci) {
        if (isCompressorActive && newMrPress != 0) {
            float deltaT = MTRClient.getLastFrameDuration() / 20.0F;
            newMrPress += deltaT * 5.0F;
            mrPress = (int) newMrPress;
        } else {
            newMrPress = mrPress;
        }
    }

}
