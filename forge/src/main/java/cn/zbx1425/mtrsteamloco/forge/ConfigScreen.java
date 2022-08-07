package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.CycleOption;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
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

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen)));
    }

    public ConfigScreen(Screen parentScreen) {
        super(new TextComponent("MTRSteamLoco 渲染配置"));
        this.parentScreen = parentScreen;
    }

    @Override
    public void onClose() {
        RenderConfigForge.apply();
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    protected void init() {
        int listLeft = (this.width - 400) / 2;
        WidgetBetterCheckbox enableRail3D = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 3 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("立体轨道模型"),  RenderConfigForge.CONFIG.enableRail3D::set
        );
        WidgetLabel labelEnableRail3D = new WidgetLabel(
                listLeft + 24, OPTIONS_LIST_TOP_HEIGHT + 4 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("关闭时将显示另一种平面的轨道（但平面轨道并不比立体轨道节省性能）。\n请依照喜好选择。")
        );
        WidgetBetterCheckbox shaderCompatMode = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 0 * OPTIONS_LIST_ITEM_HEIGHT,400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("光影兼容模式"), checked -> {
                    RenderConfigForge.CONFIG.shaderCompatMode.set(checked);
                labelEnableRail3D.visible = enableRail3D.visible = !checked;
        });
        WidgetBetterCheckbox enableRailRender = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 7 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示轨道"),  RenderConfigForge.CONFIG.enableRailRender::set
        );
        WidgetBetterCheckbox enableTrainRender = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 8 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示列车"),  RenderConfigForge.CONFIG.enableTrainRender::set
        );
        WidgetBetterCheckbox enableSmoke = new WidgetBetterCheckbox(
                listLeft, OPTIONS_LIST_TOP_HEIGHT + 5 * OPTIONS_LIST_ITEM_HEIGHT, 400, OPTIONS_LIST_ITEM_HEIGHT,
                new TextComponent("显示蒸汽机车的烟雾"),  RenderConfigForge.CONFIG.enableSmoke::set
        );
        shaderCompatMode.setChecked(RenderConfigForge.CONFIG.shaderCompatMode.get());
        enableRail3D.setChecked(RenderConfigForge.CONFIG.enableRail3D.get());
        enableRailRender.setChecked(RenderConfigForge.CONFIG.enableRailRender.get());
        enableTrainRender.setChecked(RenderConfigForge.CONFIG.enableTrainRender.get());
        enableSmoke.setChecked(RenderConfigForge.CONFIG.enableSmoke.get());
        labelEnableRail3D.visible = enableRail3D.visible = !RenderConfigForge.CONFIG.shaderCompatMode.get();
        this.addRenderableWidget(shaderCompatMode);
        this.addRenderableWidget(enableRail3D);
        this.addRenderableWidget(enableRailRender);
        this.addRenderableWidget(enableTrainRender);

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