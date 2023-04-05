package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mtr.data.IGui;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Consumer;
import java.util.function.Function;

public class WidgetSlider extends AbstractSliderButton implements IGui {

    private final int maxValue;
    private final Function<Integer, String> setMessage;

    private static final int SLIDER_WIDTH = 10;

    public WidgetSlider(int maxValue, int value, Function<Integer, String> setMessage) {
        super(0, 0, 0, 20, Text.literal(""), 0);
        this.maxValue = maxValue;
        this.setMessage = setMessage;
        this.setValue(value);
    }

#if MC_VERSION <= "11903"
    @Override
    public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
        final Minecraft client = Minecraft.getInstance();
        UtilitiesClient.beginDrawingTexture(WIDGETS_LOCATION);

        blit(matrices, UtilitiesClient.getWidgetX(this), UtilitiesClient.getWidgetY(this), 0, 46, width / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this), UtilitiesClient.getWidgetY(this) + height / 2, 0, 66 - height / 2, width / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this) + width / 2, UtilitiesClient.getWidgetY(this), 200 - width / 2, 46, width / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this) + width / 2, UtilitiesClient.getWidgetY(this) + height / 2, 200 - width / 2, 66 - height / 2, width / 2, height / 2);

        final int v = UtilitiesClient.isHovered(this) ? 86 : 66;
        final int xOffset = (width - SLIDER_WIDTH) * getIntValue() / maxValue;
        blit(matrices, UtilitiesClient.getWidgetX(this) + xOffset, UtilitiesClient.getWidgetY(this), 0, v, SLIDER_WIDTH / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this) + xOffset, UtilitiesClient.getWidgetY(this) + height / 2, 0, v + 20 - height / 2, SLIDER_WIDTH / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this) + xOffset + SLIDER_WIDTH / 2, UtilitiesClient.getWidgetY(this), 200 - SLIDER_WIDTH / 2, v, SLIDER_WIDTH / 2, height / 2);
        blit(matrices, UtilitiesClient.getWidgetX(this) + xOffset + SLIDER_WIDTH / 2, UtilitiesClient.getWidgetY(this) + height / 2, 200 - SLIDER_WIDTH / 2, v + 20 - height / 2, SLIDER_WIDTH / 2, height / 2);

        drawCenteredString(matrices, client.font, getMessage().getString(), UtilitiesClient.getWidgetX(this) + width / 2, UtilitiesClient.getWidgetY(this) + (height - TEXT_HEIGHT) / 2, ARGB_WHITE);
    }
#endif

    @Override
    protected void updateMessage() {
        setMessage(Text.literal(setMessage.apply(getIntValue())));
    }

    @Override
    protected void applyValue() {
    }

    public void setValue(int valueInt) {
        value = (double) valueInt / maxValue;
        updateMessage();
    }

    public int getIntValue() {
        return (int) Math.round(value * maxValue);
    }
}