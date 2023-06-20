package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.mappings.Text;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;
import java.util.List;

public class WidgetScrollList extends AbstractScrollWidget {

    public final List<AbstractWidget> children = new ArrayList<>();

    public WidgetScrollList(int x, int y, int w, int h) {
        super(x, y, w, h, Text.literal(""));
    }

    @Override
    protected void renderContents(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
#if MC_VERSION >= "11903"
        poseStack.translate(this.getX(), this.getY(), 0.0);
#else
        poseStack.translate(this.x, this.y, 0.0);
#endif
        for (AbstractWidget widget : children) {
            widget.render(poseStack, mouseX, (int) (mouseY + scrollAmount()), partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AbstractWidget widget : children) {
            widget.mouseClicked(mouseX, (int) (mouseY + scrollAmount()), button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (withinContentAreaPoint(mouseX, mouseY)) {
            setFocused(true);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    protected int getInnerHeight() {
        AbstractWidget lastChild = children.isEmpty() ? null : children.get(children.size() - 1);
        if (lastChild == null) return 0;
#if MC_VERSION >= "11903"
        return lastChild.getY() + lastChild.getHeight();
#else
        return lastChild.y + lastChild.getHeight();
#endif
    }

    @Override
    protected boolean scrollbarVisible() {
        return getInnerHeight() > height;
    }

    @Override
    protected double scrollRate() {
        AbstractWidget lastChild = children.isEmpty() ? null : children.get(children.size() - 1);
        if (lastChild == null) return 0;
        return lastChild.getHeight();
    }


#if MC_VERSION >= "11903"
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) { }
#elif MC_VERSION >= "11700"
    @Override
    public void updateNarration(NarrationElementOutput arg) { }

#endif
}
