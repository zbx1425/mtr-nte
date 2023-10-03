package cn.zbx1425.mtrsteamloco.mixin;

import mtr.client.TrainClientRegistry;
import mtr.client.TrainProperties;
import mtr.mappings.Text;
import mtr.render.JonModelTrainRenderer;
import mtr.sound.JonTrainSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrainClientRegistry.class)
public class TrainClientRegistryMixin {

    @Inject(method = "getTrainProperties(Ljava/lang/String;)Lmtr/client/TrainProperties;", at = @At("HEAD"),
            cancellable = true, remap = false)
    private static void getTrainProperties(String key, CallbackInfoReturnable<TrainProperties> cir) {
        if (key.equals("$NTE_DUMMY_BLANK_PROPERTY")) {
            TrainProperties result = new TrainProperties(
                    "train_1_1", Text.translatable(""), null, null, 0, 0, 0, 0, false, false,
                    new JonModelTrainRenderer(null, "", "", ""),
                    new JonTrainSound("", new JonTrainSound.JonTrainSoundConfig(null, 0, 0.5F, false))
            );
            cir.setReturnValue(result);
        }
    }
}
