package cn.zbx1425.mtrsteamloco.mixin;

import mtr.data.Train;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Train.class)
public interface TrainAccessor {

    @Accessor(remap = false)
    List<Double> getDistances();
}
