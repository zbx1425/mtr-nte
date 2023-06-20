package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
#if MC_VERSION >= "11700"
import net.minecraft.client.gui.narration.NarrationElementOutput;
#endif
import net.minecraft.network.chat.Component;

public class WidgetLabel extends AbstractWidget {

    public boolean alignR = false;

    private final Runnable onClick;

    public WidgetLabel(int x, int y, int width, Component text) {
        super(x, y, width, 10, text);
        this.onClick = null;
    }

    public WidgetLabel(int x, int y, int width, Component text, Runnable onClick) {
        super(x, y, width, 10, text);
        this.onClick = onClick;
    }

#if MC_VERSION >= "11904"
    @Override
    public void renderWidget(PoseStack matrices, int mouseX, int mouseY, float delta) {
#else
    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
#endif
        if (!visible) return;
        String[] lines = this.getMessage().getString().split("\n");
        this.height = lines.length * 10;
        for (int i = 0; i < lines.length; ++i) {
            int textWidth = Minecraft.getInstance().font.width(lines[i]);
#if MC_VERSION >= "11903"
            int x = rtl ? this.getX() + this.getWidth() - textWidth : this.x;
            int y = this.getY() + 10 * i;
#else
            int x = alignR ? this.x + this.width - textWidth : this.x;
            int y = this.y + 10 * i;
#endif
            if (textWidth > this.width) {
                int offset = (int)(System.currentTimeMillis() / 25 % (textWidth + 40));
                vcEnableScissor(this.x, this.y, this.x + this.width, this.y + this.height);
                drawString(matrices, Minecraft.getInstance().font, lines[i], x - offset, y, -1);
                drawString(matrices, Minecraft.getInstance().font, lines[i], x + textWidth + 40 - offset, y, -1);
                RenderSystem.disableScissor();
            } else {
                drawString(matrices, Minecraft.getInstance().font, lines[i], x, y, -1);
            }
            if (!isActive()) {
                drawString(matrices, Minecraft.getInstance().font, "▶", x - 8, y, 0xffff0000);
            }
        }
    }

    @Override
    public void onClick(double d, double e) {
        super.onClick(d, e);
        if (onClick != null) onClick.run();
    }

    private static void vcEnableScissor(int x1, int y1, int x2, int y2) {
        Window window = Minecraft.getInstance().getWindow();
        int wndHeight = window.getHeight();
        double guiScale = window.getGuiScale();
        double scaledX1 = (double)x1 * guiScale;
        double scaledY1 = (double)wndHeight - (double)y2 * guiScale;
        double scaledWidth = (double)(x2 - x1) * guiScale;
        double scaledHeight = (double)(y2 - y1) * guiScale;
        RenderSystem.enableScissor((int)scaledX1, (int)scaledY1, Math.max(0, (int)scaledWidth), Math.max(0, (int)scaledHeight));
    }

#if MC_VERSION >= "11903"
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
#elif MC_VERSION >= "11700"
    @Override
    public void updateNarration(NarrationElementOutput arg) { }
#endif
}
