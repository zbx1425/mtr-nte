package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.scripting.AbstractScriptContext;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptContextManager;
import cn.zbx1425.mtrsteamloco.render.scripting.ScriptHolder;
import com.google.common.base.Splitter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
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
        for (Map.Entry<AbstractScriptContext, ScriptHolder> entry : ScriptContextManager.livingContexts) {
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
                        String.format("#%08X (%.2f ms)", context.hashCode(), context.lastExecuteDuration / 1000f),
                        10, y, 0xFFCCCCFF);
                y += lineHeight;
                for (Map.Entry<String, String> debugInfo : context.debugInfo.entrySet()) {
                    drawText(vdStuff, font, debugInfo.getKey() + ": " + debugInfo.getValue(), 20, y, 0xFFFFFFFF);
                    y += lineHeight;
                }
            }
        }

        matrices.popPose();
    }


#if MC_VERSION >= "12000"
    private static void drawText(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color);
    }
#else
    private static void drawText(PoseStack matrices, Font font, String text, int x, int y, int color) {
        font.drawShadow(matrices, text, x, y, color);
    }
#endif
}
