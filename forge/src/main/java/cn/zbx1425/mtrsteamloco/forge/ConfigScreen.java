package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
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
    private static final int OPTIONS_LIST_ITEM_HEIGHT = 25;

    /** Width of a button */
    private static final int BUTTON_WIDTH = 200;
    /** Height of a button */
    private static final int BUTTON_HEIGHT = 20;
    /** Distance from bottom of the screen to the "Done" button's top */
    private static final int DONE_BUTTON_TOP_OFFSET = 26;

    /** List of options rows shown on the screen */
    // Not a final field because this cannot be initialized in the constructor,
    // as explained below
    private OptionsList optionsRowList;

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
        this.minecraft.setScreen(parentScreen);
    }

    @Override
    protected void init() {
        // Create the options row list
        // It must be created in this method instead of in the constructor,
        // or it will not be displayed properly
        this.optionsRowList = new OptionsList(
                this.minecraft, this.width, this.height,
                OPTIONS_LIST_TOP_HEIGHT,
                this.height - OPTIONS_LIST_BOTTOM_OFFSET,
                OPTIONS_LIST_ITEM_HEIGHT
        );

        this.optionsRowList.addBig(CycleOption.create(
                "轨道渲染方式",
                List.of(0, 1, 2),
                value -> new TextComponent(RenderConfigForge.RenderLevel.values()[value].descriptionRail),
                options -> RenderConfigForge.CONFIG.railRenderLevel.get().ordinal(),
                (arg, arg2, value) -> {
                    RenderConfigForge.CONFIG.railRenderLevel.set(RenderConfigForge.RenderLevel.values()[value]);
                    RenderUtil.railRenderLevel = value;
                }
        ));
        this.optionsRowList.addBig(CycleOption.create(
                "列车渲染方式",
                List.of(0, 1, 2),
                value -> new TextComponent(RenderConfigForge.RenderLevel.values()[value].descriptionTrain),
                options -> RenderConfigForge.CONFIG.trainRenderLevel.get().ordinal(),
                (arg, arg2, value) -> {
                    RenderConfigForge.CONFIG.trainRenderLevel.set(RenderConfigForge.RenderLevel.values()[value]);
                    RenderUtil.trainRenderLevel = value;
                }
        ));

        // Add the options row list as this screen's child
        // If this is not done, users cannot click on items in the list
        this.addWidget(this.optionsRowList);

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
        this.optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredString(matrixStack, this.font, this.title.getString(),
                this.width / 2, TITLE_HEIGHT, 0xFFFFFF);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}