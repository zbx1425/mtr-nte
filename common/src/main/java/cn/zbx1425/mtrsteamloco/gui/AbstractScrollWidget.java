package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractScrollWidget extends AbstractWidget {
    private double offset;
    private boolean holdingScrollBar;

    public AbstractScrollWidget(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible) return false;
        boolean clickInside = this.isMouseInside(mouseX, mouseY);
        boolean clickBar = this.getScrollBarVisible() && mouseX >= (double)(this.x + this.width) && mouseX <= (double)(this.x + this.width + 8) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
        this.setFocused(clickInside || clickBar);
        if (clickBar && button == 0) {
            this.holdingScrollBar = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.holdingScrollBar = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!(this.visible && this.isFocused() && this.holdingScrollBar)) return false;
        if (mouseY < (double)this.y) {
            this.setOffset(0.0);
        } else if (mouseY > (double)(this.y + this.height)) {
            this.setOffset(this.getMaxOffset());
        } else {
            int i = this.getScrollBarHeight();
            double d = Math.max(1, this.getMaxOffset() / (this.height - i));
            this.setOffset(this.offset + dragY * d);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!this.visible || !this.isFocused()) return false;
        this.setOffset(this.offset - delta * this.getScrollInterval());
        return true;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        this.renderBackground(poseStack);
        vcEnableScissor(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1);
        poseStack.pushPose();
        poseStack.translate(0.0, -this.offset, 0.0);
        this.renderContents(poseStack, mouseX, mouseY, partialTick);
        poseStack.popPose();
        RenderSystem.disableScissor();
        if (this.getScrollBarVisible()) {
            this.renderScrollBar();
        }
    }

    public static void vcEnableScissor(int x1, int y1, int x2, int y2) {
        Window window = Minecraft.getInstance().getWindow();
        int wndHeight = window.getHeight();
        double guiScale = window.getGuiScale();
        double scaledX1 = (double)x1 * guiScale;
        double scaledY1 = (double)wndHeight - (double)y2 * guiScale;
        double scaledWidth = (double)(x2 - x1) * guiScale;
        double scaledHeight = (double)(y2 - y1) * guiScale;
        RenderSystem.enableScissor((int)scaledX1, (int)scaledY1, Math.max(0, (int)scaledWidth), Math.max(0, (int)scaledHeight));
    }

    private int getScrollBarHeight() {
        return Mth.clamp((int)((float)(this.height * this.height) / (float)this.getContentHeight()), 32, this.height);
    }

    protected double getOffset() {
        return this.offset;
    }

    protected void setOffset(double offset) {
        this.offset = Mth.clamp(offset, 0.0, this.getMaxOffset());
    }

    protected int getMaxOffset() {
        return Math.max(0, this.getContentHeight() - this.height);
    }

    private void renderBackground(PoseStack poseStack) {
        fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, this.isFocused() ? 0xffffffff : 0xffa0a0a0);
        fill(poseStack, this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, -0xff555555);
    }

    private void renderScrollBar() {
        int i = this.getScrollBarHeight();
        int j = this.x + this.width;
        int k = this.x + this.width + 8;
        int l = Math.max(this.y, (int)this.offset * (this.height - i) / this.getMaxOffset() + this.y);
        int m = l + i;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(j, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, m, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(k, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(j, m - 1, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(k - 1, m - 1, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(k - 1, l, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(j, l, 0.0).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }

    protected boolean isMouseInside(double x, double y) {
        return x >= (double)this.x && x < (double)(this.x + this.width) && y >= (double)this.y && y < (double)(this.y + this.height);
    }

    protected abstract int getContentHeight();

    protected abstract boolean getScrollBarVisible();

    protected abstract double getScrollInterval();

    protected abstract void renderContents(PoseStack var1, int var2, int var3, float var4);
}

