package cn.zbx1425.sowcer.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.client.renderer.RenderStateShard.*;

public class BlazeRenderType {

    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false, false))
                .setTransparencyState(NO_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                .createCompositeState(true);
        return RenderType.create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, compositeState);
    });
    private static final Function<ResourceLocation, RenderType> ENTITY_TRANSLUCENT_CULL = Util.memoize(resourceLocation -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                .createCompositeState(true);
        return RenderType.create("entity_translucent_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, true, compositeState);
    });
    private static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize((resourceLocation, translucent) -> {
        RenderType.CompositeState compositeState = RenderType.CompositeState.builder()
                .setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard((ResourceLocation)resourceLocation, false, false))
                .setTransparencyState(translucent ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY).setWriteMaskState(translucent ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return RenderType.create("beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.TRIANGLES, 256, false, true, compositeState);
    });

    public static RenderType entityCutout(ResourceLocation resourceLocation) {
        return ENTITY_CUTOUT.apply(resourceLocation);
    }

    public static RenderType entityTranslucentCull(ResourceLocation resourceLocation) {
        return ENTITY_TRANSLUCENT_CULL.apply(resourceLocation);
    }

    public static RenderType beaconBeam(ResourceLocation resourceLocation, boolean bl) {
        return BEACON_BEAM.apply(resourceLocation, bl);
    }
}
