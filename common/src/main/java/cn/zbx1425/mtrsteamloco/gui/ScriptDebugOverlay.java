package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptContextManager;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import cn.zbx1425.mtrsteamloco.render.scripting.util.GraphicsTexture;
import com.google.common.base.Splitter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#else
import net.minecraft.client.gui.GuiComponent;
#endif
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptDebugOverlay {

#if MC_VERSION >= "12000"
    public static void render(GuiGraphics vdStuff) {
        PoseStack matrices = vdStuff.pose();
#else
    public static void render(PoseStack vdStuff) {
        PoseStack matrices = vdStuff;
#endif
        if (!ClientConfig.enableScriptDebugOverlay) return;
        if (Minecraft.getInstance().screen != null) return;

        matrices.pushPose();
        matrices.translate(10, 10, 0);

        Map<ScriptHolder, List<AbstractScriptContext>> contexts = new HashMap<>();
        for (Map.Entry<AbstractScriptContext, ScriptHolder> entry : ScriptContextManager.livingContexts.entrySet()) {
            contexts.computeIfAbsent(entry.getValue(), k -> new java.util.ArrayList<>()).add(entry.getKey());
        }

        int y = 0;
        Font font = Minecraft.getInstance().font;
        int lineHeight = Mth.ceil(font.lineHeight * 1.2f);
        for (Map.Entry<ScriptHolder, List<AbstractScriptContext>> entry : contexts.entrySet()) {
            ScriptHolder holder = entry.getKey();
            if (holder.failTime > 0) {
                drawText(vdStuff, font, holder.name + " FAILED", 0, y, 0xFFFF0000);
                y += lineHeight;
                for (String msgLine : Splitter.fixedLength(60).split(holder.failException.getMessage())) {
                    drawText(vdStuff, font, msgLine, 5, y, 0xFFFF8888);
                    y += lineHeight;
                }
            } else {
                drawText(vdStuff, font, holder.name, 0, y, 0xFFAAAAFF);
                y += lineHeight;
            }
            for (AbstractScriptContext context : entry.getValue()) {
                drawText(vdStuff, font,
                        String.format("#%08X (%.2f ms)", context.hashCode(), context.lastExecuteDuration / 1000.0),
                        10, y, 0xFFCCCCFF);
                y += lineHeight;
                for (Map.Entry<String, Object> debugInfo : context.debugInfo.entrySet()) {
                    Object value = debugInfo.getValue();
                    if (value instanceof GraphicsTexture) {
                        GraphicsTexture texture = (GraphicsTexture) value;
                        float scale = (Minecraft.getInstance().getWindow().getGuiScaledWidth() - 40) / (float) texture.width;
                        blit(vdStuff, texture.identifier, 20, y, (int)(texture.width * scale), (int)(texture.height * scale));
                        drawText(vdStuff, font, debugInfo.getKey() + ": GraphicsTexture", 20, y, 0xFFFFFFFF);
                        y += (int)(texture.height * scale) + lineHeight / 2;
                    } else {
                        drawText(vdStuff, font, debugInfo.getKey() + ": " + debugInfo.getValue(), 20, y, 0xFFFFFFFF);
                        y += lineHeight;
                    }
                }
            }
        }

        matrices.popPose();
    }


#if MC_VERSION >= "12000"
    private static void drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color);
    }
    private static void blit(GuiGraphics guiGraphics, ResourceLocation texture, int x, int y, int width, int height) {
        guiGraphics.blit(texture, x, y, width, height, 0, 0, 1, 1, 1, 1);
    }
#else
    private static void drawText(PoseStack matrices, Font font, String text, int x, int y, int color) {
        font.drawShadow(matrices, text, x, y, color);
    }
    private static void blit(PoseStack matrices, ResourceLocation texture, int x, int y, int width, int height) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(matrices, x, y, width, height, 0, 0, 1, 1, 1, 1);
    }
#endif
}
