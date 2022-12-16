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

    public WidgetLabel(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, text);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
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

#if MC_VERSION >= "11903"
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
#elif MC_VERSION >= "11700"
    @Override
    public void updateNarration(NarrationElementOutput arg) { }
#endif
}
