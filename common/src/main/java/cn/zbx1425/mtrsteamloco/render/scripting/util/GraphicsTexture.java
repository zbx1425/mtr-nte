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
import java.util.UUID;

@SuppressWarnings("unused")
public class GraphicsTexture {

    private final DynamicTexture dynamicTexture;
    public final ResourceLocation identifier;

    private final BufferedImage bufferedImage;
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
        ComponentColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, width, height, width * 4, 4, new int[] {0, 1, 2, 3}, null); // R, G, B, A order
        bufferedImage = new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
        graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public void upload() {
        byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        long pixelAddr = ((NativeImageAccessor)(Object)dynamicTexture.getPixels()).getPixels();
        MemoryUtil.memByteBuffer(pixelAddr, pixels.length).put(pixels);
        RenderSystem.recordRenderCall(dynamicTexture::upload);
    }

}
