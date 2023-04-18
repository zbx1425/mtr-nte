package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.data.IGui;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
#if MC_VERSION >= "11700"
import net.minecraft.client.gui.narration.NarrationElementOutput;
#endif
import net.minecraft.network.chat.Component;

public class WidgetLabel extends AbstractWidget {

    private final Runnable onClick;

    public WidgetLabel(int x, int y, int width, Component text) {
        super(x, y, width, 8, text);
        this.onClick = null;
    }

    public WidgetLabel(int x, int y, int width, Component text, Runnable onClick) {
        super(x, y, width, 8, text);
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
        for (int i = 0; i < lines.length; ++i) {
#if MC_VERSION >= "11903"
            drawString(matrices, Minecraft.getInstance().font, lines[i], this.getX(), this.getY() + 10 * i, -1);
#else
            drawString(matrices, Minecraft.getInstance().font, lines[i], this.x, this.y + 10 * i, -1);
#endif
        }
    }

    @Override
    public void onClick(double d, double e) {
        super.onClick(d, e);
        if (onClick != null) onClick.run();
    }

#if MC_VERSION >= "11903"
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
#elif MC_VERSION >= "11700"
    @Override
    public void updateNarration(NarrationElementOutput arg) { }
#endif
}
