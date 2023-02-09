package cn.zbx1425.mtrsteamloco.render.font;

import cn.zbx1425.mtrsteamloco.Main;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.codec.binary.Hex;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.AttributedString;

public class FontTexture implements Closeable {

    public final ResourceLocation resourceLocation;
    public final int width, height;

    public double lastUseTime;

    private static final int FONT_SIZE = 32;

    public FontTexture(String text) throws IOException {
        Font font = FontTextureCache.FONT_SANS.deriveFont(Font.PLAIN, FONT_SIZE);
        Rectangle2D strBounds = font.getStringBounds(text, FontTextureCache.FONT_CONTEXT);
        LineMetrics lineMetrics = font.getLineMetrics(text, FontTextureCache.FONT_CONTEXT);

        if (strBounds.getWidth() == 0 || strBounds.getHeight() == 0) {
            width = 0; height = 0; resourceLocation = null;
            return;
        }

        BufferedImage awtImage = new BufferedImage((int)strBounds.getWidth(), (int)strBounds.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = awtImage.createGraphics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, font);
        graphics2D.drawString(attributedString.getIterator(), 0, lineMetrics.getAscent());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(awtImage, "png", bos);
        NativeImage nativeImage;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            ByteBuffer byteBuffer = memoryStack.malloc(bos.size());
            byteBuffer.put(bos.toByteArray());
            byteBuffer.rewind();
            nativeImage = NativeImage.read(byteBuffer);
        }
        width = nativeImage.getWidth();
        height = nativeImage.getHeight();
        DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);

        resourceLocation = new ResourceLocation("mtrsteamloco", "text_textures/" + Hex.encodeHexString(text.getBytes(StandardCharsets.UTF_8)));
        Minecraft.getInstance().getTextureManager().register(resourceLocation, dynamicTexture);

        graphics2D.dispose();
    }

    @Override
    public void close() {
        if (resourceLocation == null) return;

        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
        if (abstractTexture != MissingTextureAtlasSprite.getTexture()) {
            try {
                abstractTexture.close();
            } catch (Exception exception) {
                Main.LOGGER.warn("Failed to close texture {}", resourceLocation, exception);
            }
        }
    }
}
