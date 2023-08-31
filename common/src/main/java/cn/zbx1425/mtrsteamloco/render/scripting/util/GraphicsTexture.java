package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.mixin.NativeImageAccessor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.UUID;

@SuppressWarnings("unused")
public class GraphicsTexture implements Closeable {

    private final DynamicTexture dynamicTexture;
    public final ResourceLocation identifier;

    public final BufferedImage bufferedImage;
    public final Graphics2D graphics;

    public final int width, height;

    public GraphicsTexture(int width, int height) {
        this.width = width;
        this.height = height;
        dynamicTexture = new DynamicTexture(new NativeImage(width, height, false));
        identifier = new ResourceLocation(Main.MOD_ID, String.format("dynamic/graphics/%s", UUID.randomUUID()));
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getTextureManager().register(identifier, dynamicTexture);
        });
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    public static BufferedImage createArgbBufferedImage(BufferedImage src) {
        BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = newImage.createGraphics();
        graphics.drawImage(src, 0, 0, null);
        graphics.dispose();
        return newImage;
    }

    public void upload() {
        IntBuffer imgData = IntBuffer.wrap(((DataBufferInt)bufferedImage.getRaster().getDataBuffer()).getData());
        long pixelAddr = ((NativeImageAccessor)(Object)dynamicTexture.getPixels()).getPixels();
        ByteBuffer target = MemoryUtil.memByteBuffer(pixelAddr, width * height * 4);
        for (int i = 0; i < width * height; i++) {
            // ARGB to RGBA
            int pixel = imgData.get();
            target.put((byte)((pixel >> 16) & 0xFF));
            target.put((byte)((pixel >> 8) & 0xFF));
            target.put((byte)(pixel & 0xFF));
            target.put((byte)((pixel >> 24) & 0xFF));
        }
        RenderSystem.recordRenderCall(dynamicTexture::upload);
    }

    @Override
    public void close() {
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().getTextureManager().release(identifier);
        });
        graphics.dispose();
    }
}
