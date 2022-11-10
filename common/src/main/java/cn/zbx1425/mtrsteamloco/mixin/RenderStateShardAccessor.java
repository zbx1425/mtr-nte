package cn.zbx1425.mtrsteamloco.mixin;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {

    @Accessor @Final
    static RenderStateShard.TransparencyStateShard getNO_TRANSPARENCY() { return null; }

    @Accessor @Final
    static RenderStateShard.TransparencyStateShard getTRANSLUCENT_TRANSPARENCY() { return null; }

    @Accessor @Final
    static RenderStateShard.ShaderStateShard getRENDERTYPE_ENTITY_CUTOUT_SHADER() { return null; }

    @Accessor @Final
    static RenderStateShard.ShaderStateShard getRENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER() { return null; }

    @Accessor @Final
    static RenderStateShard.ShaderStateShard getRENDERTYPE_BEACON_BEAM_SHADER() { return null; }


}
