package cn.zbx1425.mtrsteamloco.mixin;

import mtr.client.ClientCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.awt.*;

@Mixin(value = ClientCache.class, remap = false)
public interface ClientCacheAccessor {

    @Accessor
    Font getFont();

    @Accessor
    void setFont(Font value);

    @Accessor
    Font getFontCjk();

    @Accessor
    void setFontCjk(Font value);
}
