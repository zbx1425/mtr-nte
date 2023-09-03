package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.client.IDrawing;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
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

    public static Screen createScreen(Screen parent) {
        try {
            Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
            return ClothLogic.createClothScreen(parent);
        } catch (Exception ignored) {
            return new ConfigScreen(parent);
        }
    }

    private static class ClothLogic {

        public static Screen createClothScreen(Screen parent) {
            return ClothConfigScreen.createScreen(parent);
        }
    }

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
        this.clearWidgets();

        int listLeft = (this.width - 400) / 2;
        WidgetBetterCheckbox enableRail3D = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 1 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.rail3d"), checked -> {
            ClientConfig.enableRail3D = checked;
            Minecraft.getInstance().levelRenderer.allChanged();
        }
        );
        WidgetLabel labelEnableRail3D = new WidgetLabel(
                listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 2 * OPTIONS_LIST_ITEM_HEIGHT, 400,
                Text.translatable("gui.mtrsteamloco.config.client.rail3d.description")
        );
        WidgetBetterCheckbox shaderCompatMode = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 0 * OPTIONS_LIST_ITEM_HEIGHT,400, OPTIONS_LIST_ITEM_HEIGHT,
                Text.translatable("gui.mtrsteamloco.config.client.shadercompat"), checked -> {
            ClientConfig.enableOptimization = checked;
            labelEnableRail3D.visible = enableRail3D.visible = ClientConfig.useRenderOptimization();
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
        shaderCompatMode.setChecked(ClientConfig.enableOptimization);
        enableRail3D.setChecked(ClientConfig.enableRail3D);
        enableRailRender.setChecked(ClientConfig.enableRailRender);
        enableTrainRender.setChecked(ClientConfig.enableTrainRender);
        enableSmoke.setChecked(ClientConfig.enableSmoke);
        hideRidingTrain.setChecked(ClientConfig.hideRidingTrain);
        labelEnableRail3D.visible = enableRail3D.visible = ClientConfig.useRenderOptimization();
        this.addRenderableWidget(shaderCompatMode);
        this.addRenderableWidget(enableRail3D);
        this.addRenderableWidget(enableRailRender);
        this.addRenderableWidget(enableTrainRender);
        this.addRenderableWidget(hideRidingTrain);
        this.addRenderableWidget(enableSmoke);

        if (!ShadersModHandler.canDrawWithBuffer()) {
            this.addRenderableWidget(new WidgetLabel(
                    listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 1 * OPTIONS_LIST_ITEM_HEIGHT, 400,
                    Text.translatable("gui.mtrsteamloco.config.client.shaderactive")
            ));
        }

        this.addRenderableWidget(labelEnableRail3D);
        this.addRenderableWidget(new WidgetLabel(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 4 * OPTIONS_LIST_ITEM_HEIGHT, 400,
                Text.translatable("gui.mtrsteamloco.config.client.railrender.description")
        ));

        // Add the "Done" button
        Button btnDone = UtilitiesClient.newButton(CommonComponents.GUI_DONE, button -> this.onClose());
        IDrawing.setPositionAndWidth(btnDone, (this.width - BUTTON_WIDTH) / 2, this.height - DONE_BUTTON_TOP_OFFSET, BUTTON_WIDTH);
        this.addRenderableWidget(btnDone);
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
#else
    public void render(PoseStack guiGraphics, int mouseX, int mouseY, float partialTicks) {
#endif
        this.renderBackground(guiGraphics);
        // Options row list must be rendered here,
        // otherwise the GUI will be broken
#if MC_VERSION >= "12000"
        guiGraphics.drawCenteredString(this.font, this.title.getString(),
                this.width / 2, TITLE_HEIGHT, 0xFFFFFF);
#else
        drawCenteredString(guiGraphics, this.font, this.title.getString(),
                this.width / 2, TITLE_HEIGHT, 0xFFFFFF);
#endif
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
