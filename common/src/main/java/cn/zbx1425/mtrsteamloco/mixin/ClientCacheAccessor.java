package cn.zbx1425.mtrsteamloco.mixin;

import mtr.client.ClientCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.awt.*;

@Mixin(ClientCache.class)
public interface ClientCacheAccessor {

    @Accessor
    Font getFont();

    @Accessor
    Font getFontCjk();
}
