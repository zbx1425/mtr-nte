package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
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
        boolean clickBar = this.getScrollBarVisible() && mouseX >= (double)(this.getX() + this.width) && mouseX <= (double)(this.getX() + this.width + 8) && mouseY >= (double)this.getY() && mouseY < (double)(this.getY() + this.height);
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
        if (mouseY < (double)this.getY()) {
            this.setOffset(0.0);
        } else if (mouseY > (double)(this.getY() + this.height)) {
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
#if MC_VERSION >= "12000"
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics.pose();
#elif MC_VERSION >= "11904"
    public void renderWidget(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics;
#else
    public void renderButton(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
        PoseStack poseStack = guiGraphics;
#endif
        if (!this.visible) {
            return;
        }
        this.renderBackground(guiGraphics);
        vcEnableScissor(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        poseStack.pushPose();
        poseStack.translate(0.0, -this.offset, 0.0);
        this.renderContents(guiGraphics, mouseX, mouseY, partialTick);
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

#if MC_VERSION >= "12000"
    private void renderBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(this.getX(), this.getY() + 1, this.getX() + this.width, this.getY() + this.height - 1, this.isFocused() ? 0xffffffff : 0xffa0a0a0);
        guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xff555555);
    }
#else
    private void renderBackground(PoseStack poseStack) {
        fill(poseStack, this.getX(), this.getY() + 1, this.getX() + this.width, this.getY() + this.height - 1, this.isFocused() ? 0xffffffff : 0xffa0a0a0);
        fill(poseStack, this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, 0xff555555);
    }
#endif

    private void renderScrollBar() {
        int i = this.getScrollBarHeight();
        int j = this.getX() + this.width;
        int k = this.getX() + this.width + 8;
        int l = Math.max(this.getY(), (int)this.offset * (this.height - i) / this.getMaxOffset() + this.getY());
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
        return x >= (double)this.getX() && x < (double)(this.getX() + this.width) && y >= (double)this.getY() && y < (double)(this.getY() + this.height);
    }

    protected abstract int getContentHeight();

    protected abstract boolean getScrollBarVisible();

    protected abstract double getScrollInterval();

#if MC_VERSION >= "12000"
    protected abstract void renderContents(GuiGraphics var1, int var2, int var3, float var4);
#else
    protected abstract void renderContents(PoseStack var1, int var2, int var3, float var4);
#endif

#if MC_VERSION < "11903"
    protected int getX() {
        return x;
    }

    protected int getY() {
        return y;
    }
#endif
}

