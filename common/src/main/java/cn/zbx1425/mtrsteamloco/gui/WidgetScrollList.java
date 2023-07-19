package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.mappings.Text;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;

public class WidgetScrollList extends AbstractScrollWidget {

    public final ArrayList<AbstractWidget> children = new ArrayList<>();

    public WidgetScrollList(int x, int y, int w, int h) {
        super(x, y, w, h, Text.literal(""));
    }

    @Override
#if MC_VERSION >= "12000"
    protected void renderContents(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
#else
    protected void renderContents(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics;
#endif
        poseStack.translate(this.getX(), this.getY(), 0.0);
        for (AbstractWidget widget : children) {
            widget.render(guiGraphics, mouseX - this.getX(), (int) (mouseY + getOffset()) - this.getY(), partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : children) {
            widget.mouseClicked(mouseX - this.getX(), (int) (mouseY + getOffset()) - this.getY(), button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (isMouseInside(mouseX, mouseY)) {
            setFocused(true);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    protected int getContentHeight() {
        AbstractWidget lastChild = children.isEmpty() ? null : children.get(children.size() - 1);
        if (lastChild == null) return 0;
#if MC_VERSION >= "11903"
        return lastChild.getY() + lastChild.getHeight();
#else
        return lastChild.y + lastChild.getHeight();
#endif
    }

    @Override
    protected boolean getScrollBarVisible() {
        return getContentHeight() > height;
    }

    @Override
    protected double getScrollInterval() {
        AbstractWidget lastChild = children.isEmpty() ? null : children.get(children.size() - 1);
        if (lastChild == null) return 0;
        return lastChild.getHeight();
    }

    public void setHeight(int height) {
        this.height = height;
    }


#if MC_VERSION >= "11903"
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
#elif MC_VERSION >= "11700"
    @Override
    public void updateNarration(NarrationElementOutput arg) { }

#endif
}
