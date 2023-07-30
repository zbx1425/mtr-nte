package cn.zbx1425.mtrsteamloco.render.scripting;

import cn.zbx1425.mtrsteamloco.render.font.FontTextureCache;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import cn.zbx1425.sowcerext.util.ResourceUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.awt.*;
import java.io.IOException;

public class ScriptResourceUtil {

    public static ResourceLocation relativeBase;

    public static ResourceManager manager() {
        return MtrModelRegistryUtil.resourceManager;
    }

    public static ResourceLocation identifier(String textForm) {
        return new ResourceLocation(textForm);
    }

    public static ResourceLocation idRelative(String textForm) {
        return ResourceUtil.resolveRelativePath(relativeBase, textForm, null);
    }

    public static String readString(ResourceLocation identifier) {
        try {
            return ResourceUtil.readResource(manager(), identifier);
        } catch (IOException e) {
            return null;
        }
    }

    public static Font getBuiltinFont(boolean supportCjk, boolean serif) {
        return FontTextureCache.FONT_SERIF;
    }
}
