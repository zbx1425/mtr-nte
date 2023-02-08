package cn.zbx1425.mtrsteamloco.render.display;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FontTextureCache {

    public static Font FONT_SANS;

    private static final HashMap<String, FontTexture> textures = new HashMap<>();

    private static double lastCheckTime = 0;

    public static FontTexture getTexture(String text) {
        if (text == null || text.length() == 0) return null;

        if (RenderUtil.runningSeconds - lastCheckTime > 10) {
            lastCheckTime = RenderUtil.runningSeconds;
            for (var it = textures.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, FontTexture> entry = it.next();
                if (RenderUtil.runningSeconds - entry.getValue().lastUseTime > 60) {
                    entry.getValue().close();
                    it.remove();
                }
            }
        }

        if (textures.containsKey(text)) {
            FontTexture result = textures.get(text);
            result.lastUseTime = RenderUtil.runningSeconds;
            if (result.resourceLocation == null) return null;
            return result;
        } else {
            try {
                FontTexture result = new FontTexture(text);
                result.lastUseTime = RenderUtil.runningSeconds;
                textures.put(text, result);
                if (result.resourceLocation == null) return null;
                return result;
            } catch (Exception ex) {
                Main.LOGGER.error("Failed generating font texture: ", ex);
                return null;
            }
        }
    }

    public static void reloadFont(ResourceManager resourceManager) {
        for (FontTexture texture : textures.values()) {
            texture.close();
        }
        textures.clear();

        try {
            FONT_SANS = Font.createFont(0, Utilities.getInputStream(resourceManager.getResource(new ResourceLocation("mtrsteamloco", "font/noto-sans-cjk-tc-medium.otf"))));
        } catch (Exception ex) {
            Main.LOGGER.error("Failed loading font: ", ex);
            MtrModelRegistryUtil.loadingErrorList.add("Font" + ExceptionUtils.getStackTrace(ex));
        }
    }
}
