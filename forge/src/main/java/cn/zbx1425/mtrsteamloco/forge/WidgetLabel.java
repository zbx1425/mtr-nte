package cn.zbx1425.mtrsteamloco.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.data.IGui;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
            drawString(matrices, Minecraft.getInstance().font, lines[i], this.x, this.y + 10 * i, -1);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput arg) {

    }
}
