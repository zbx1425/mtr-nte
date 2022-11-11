package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.mappings.Text;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;

public final class ConfigScreen extends Screen {
    /**
     * Distance between this GUI's title and the top of the screen
     */
    private static final int TITLE_HEIGHT = 8;

    /** Distance from top of the screen to the options row list's top */
    private static final int OPTIONS_LIST_TOP_HEIGHT = 24;
    /** Distance from bottom of the screen to the options row list's bottom */
    private static final int OPTIONS_LIST_BOTTOM_OFFSET = 32;
    /** Height of each item in the options row list */
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 20;

    /** Width of a button */
    private static final int BUTTON_WIDTH = 200;
    /** Height of a button */
    private static final int BUTTON_HEIGHT = 20;
    /** Distance from bottom of the screen to the "Done" button's top */
    private static final int DONE_BUTTON_TOP_OFFSET = 26;

    private final Screen parentScreen;

    public ConfigScreen(Screen parentScreen) {
        super(Text.translatable("gui.mtrsteamloco.config.client.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void onClose() {
        ClientConfig.save();
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    protected void init() {
        int listLeft = (this.width - 400) / 2;
        WidgetBetterCheckbox enableRail3D = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 1 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.rail3d"), checked -> ClientConfig.enableRail3D = checked
        );
        WidgetLabel labelEnableRail3D = new WidgetLabel(
                listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 2 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.rail3d.description")
        );
        WidgetBetterCheckbox shaderCompatMode = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 0 * OPTIONS_LIST_ITEM_HEIGHT,400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.shadercompat"), checked -> {
                    ClientConfig.shaderCompatMode = checked;
                labelEnableRail3D.visible = enableRail3D.visible = !checked;
        });
        WidgetBetterCheckbox enableTrainRender = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 5 * OPTIONS_LIST_ITEM_HEIGHT, 200, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.trainrender"),  checked -> ClientConfig.enableTrainRender = checked
        );
        WidgetBetterCheckbox enableRailRender = new WidgetBetterCheckbox(
                listLeft + 200, OPTIONS_LIST_TOP_HEIGHT + 5 * OPTIONS_LIST_ITEM_HEIGHT, 200, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.railrender"),  checked -> ClientConfig.enableRailRender = checked
        );
        WidgetBetterCheckbox hideRidingTrain = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 6 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.hideridingtrain"),  checked -> ClientConfig.hideRidingTrain = checked
        );
        WidgetBetterCheckbox enableSmoke = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 3 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.slsmoke"),  checked -> ClientConfig.enableSmoke = checked
        );
        shaderCompatMode.setChecked(ClientConfig.shaderCompatMode);
        enableRail3D.setChecked(ClientConfig.enableRail3D);
        enableRailRender.setChecked(ClientConfig.enableRailRender);
        enableTrainRender.setChecked(ClientConfig.enableTrainRender);
        enableSmoke.setChecked(ClientConfig.enableSmoke);
        hideRidingTrain.setChecked(ClientConfig.hideRidingTrain);
        labelEnableRail3D.visible = enableRail3D.visible = !(ClientConfig.shaderCompatMode || ShadersModHandler.isShaderPackInUse());
        this.addRenderableWidget(enableRail3D);
        this.addRenderableWidget(enableRailRender);
        this.addRenderableWidget(enableTrainRender);
        this.addRenderableWidget(hideRidingTrain);
        this.addRenderableWidget(enableSmoke);

        if (ShadersModHandler.isShaderPackInUse()) {
            this.addRenderableWidget(new WidgetLabel(
                    listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 0 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                    Text.translatable("gui.mtrsteamloco.config.client.shaderactive")
            ));
        } else {
            this.addRenderableWidget(shaderCompatMode);
            /* this.addRenderableWidget(new WidgetLabel(
                    listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 1 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                    Text.translatable("gui.mtrsteamloco.config.client.shadercompat.description")
            ));*/
        }

        this.addRenderableWidget(labelEnableRail3D);
        this.addRenderableWidget(new WidgetLabel(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 4 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.railrender.description")
        ));

        // Add the "Done" button
        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2,
                this.height - DONE_BUTTON_TOP_OFFSET,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                // Text shown on the button
                CommonComponents.GUI_DONE,
                // Action performed when the button is pressed
                button -> this.onClose()
        ));
    }

    @Override
    public void render(PoseStack matrixStack,
                       int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        // Options row list must be rendered here,
        // otherwise the GUI will be broken
        drawCenteredString(matrixStack, this.font, this.title.getString(),
                this.width / 2, TITLE_HEIGHT, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}