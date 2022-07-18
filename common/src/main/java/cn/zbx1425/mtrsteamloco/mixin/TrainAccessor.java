package cn.zbx1425.mtrsteamloco.mixin;

import mtr.path.PathData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(mtr.data.Train.class)
public interface TrainAccessor {

    @Accessor(remap = false)
    float getStopCounter();

    @Accessor(remap = false)
    int getNextStoppingIndex();

    @Accessor(remap = false)
    List<PathData> getPath();
}
