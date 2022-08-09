package cn.zbx1425.mtrsteamloco.gui;

import ca.weblite.objc.Client;
import cn.zbx1425.mtrsteamloco.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

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
        super(new TextComponent("MTRSteamLoco 渲染配置"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void onClose() {
        ClientConfig.applyAndSave();
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    protected void init() {
        int listLeft = (this.width - 400) / 2;
        WidgetBetterCheckbox enableRail3D = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 3 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("立体轨道模型"),  checked -> ClientConfig.enableRail3D = checked
        );
        WidgetLabel labelEnableRail3D = new WidgetLabel(
                listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 4 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("关闭时将显示另一种平面的轨道（但平面轨道并不比立体轨道节省性能）。\n请依照喜好选择。")
        );
        WidgetBetterCheckbox shaderCompatMode = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 0 * OPTIONS_LIST_ITEM_HEIGHT,400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("光影兼容模式"), checked -> {
                    ClientConfig.shaderCompatMode = checked;
                labelEnableRail3D.visible = enableRail3D.visible = !checked;
        });
        WidgetBetterCheckbox enableRailRender = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 7 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示轨道"),  checked -> ClientConfig.enableRailRender = checked
        );
        WidgetBetterCheckbox enableTrainRender = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 8 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示列车"),  checked -> ClientConfig.enableTrainRender = checked
        );
        WidgetBetterCheckbox enableSmoke = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 5 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示蒸汽机车的烟雾"),  checked -> ClientConfig.enableSmoke = checked
        );
        shaderCompatMode.setChecked(ClientConfig.shaderCompatMode);
        enableRail3D.setChecked(ClientConfig.enableRail3D);
        enableRailRender.setChecked(ClientConfig.enableRailRender);
        enableTrainRender.setChecked(ClientConfig.enableTrainRender);
        enableSmoke.setChecked(ClientConfig.enableSmoke);
        labelEnableRail3D.visible = enableRail3D.visible = !ClientConfig.shaderCompatMode;
        this.addRenderableWidget(shaderCompatMode);
        this.addRenderableWidget(enableRail3D);
        this.addRenderableWidget(enableRailRender);
        this.addRenderableWidget(enableTrainRender);
        this.addRenderableWidget(enableSmoke);

        this.addRenderableWidget(new WidgetLabel(
                listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 1 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("关闭时，将显示完整视觉效果，同时使用性能优化，但不兼容光影。\n打开时，将可兼容光影，但部分视觉效果将被禁用，轨道只有平面，且性能可能大幅下降。")
        ));
        this.addRenderableWidget(labelEnableRail3D);
        this.addRenderableWidget(new WidgetLabel(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 6 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("如果您感到性能不佳，可以通过完全隐藏轨道或列车来尝试节省性能。\n缺点自然是无法看到相应的物件了。")
        ));

        // Add the "Done" button
        this.addRenderableWidget(new Button(
                (this.width - BUTTON_WIDTH) / 2,
                this.height - DONE_BUTTON_TOP_OFFSET,
                BUTTON_WIDTH, BUTTON_HEIGHT,
                // Text shown on the button
                new TextComponent("确定"),
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