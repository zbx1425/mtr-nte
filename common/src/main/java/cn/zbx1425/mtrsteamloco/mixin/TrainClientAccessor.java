package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.TrainClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(TrainClient.class)
public interface TrainClientAccessor {

    @Accessor
    List<Double> getOffset();
}
