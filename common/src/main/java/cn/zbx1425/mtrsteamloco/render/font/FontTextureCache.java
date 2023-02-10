package cn.zbx1425.mtrsteamloco.render.font;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.display.DisplayContent;
import cn.zbx1425.mtrsteamloco.render.integration.MtrModelRegistryUtil;
import mtr.data.TrainClient;
import mtr.mappings.Utilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

public class FontTextureCache {

    public static final FontRenderContext FONT_CONTEXT = new FontRenderContext(new AffineTransform(), false, false);

    public static Font FONT_SANS;
    public static AwtFontBoundProvider FONT_SANS_BOUND_PROVIDER;

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
            FONT_SANS_BOUND_PROVIDER = new AwtFontBoundProvider(FONT_SANS);
        } catch (Exception ex) {
            Main.LOGGER.error("Failed loading font: ", ex);
            MtrModelRegistryUtil.loadingErrorList.add("Font" + ExceptionUtils.getStackTrace(ex));
        }
    }

    public static class AwtFontBoundProvider implements TextBoundProvider {

        public AwtFontBoundProvider(Font font) {
            this.font = font;
        }

        private final Font font;

        private DisplayContent context;
        private TrainClient train;

        public AwtFontBoundProvider withContext(DisplayContent context, TrainClient train) {
            this.context = context;
            this.train = train;
            return this;
        }

        @Override
        public float measureWidth(VariableText input) {
            Rectangle2D rect = font.deriveFont(0, 32).getStringBounds(input.getTargetString(context, train), FONT_CONTEXT);
            return (float)(rect.getWidth() / rect.getHeight());
        }
    }
}
