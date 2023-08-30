package cn.zbx1425.mtrsteamloco.mixin;

import mtr.mappings.ModelMapper;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelMapper.class)
public interface ModelMapperAccessor {

    @Accessor
    ModelPart getModelPart();
}
