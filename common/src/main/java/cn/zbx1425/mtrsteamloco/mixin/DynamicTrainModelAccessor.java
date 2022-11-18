package cn.zbx1425.mtrsteamloco.mixin;

import mtr.client.DynamicTrainModel;
import mtr.mappings.ModelMapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicTrainModel.class)
public interface DynamicTrainModelAccessor {

    @Accessor(remap = false)
    Map<String, ModelMapper> getParts();
}
