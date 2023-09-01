package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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

public final class ConfigScreen {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("gui.mtrsteamloco.config.client.title"))
                .transparentBackground();
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory common = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.common")
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.hideridingtrain"),
                        ClientConfig.hideRidingTrain
                ).setSaveConsumer(checked -> ClientConfig.hideRidingTrain = checked).setDefaultValue(false).build()
        );
        if (!ShadersModHandler.canDrawWithBuffer()) {
            common.addEntry(entryBuilder.startTextDescription(
                    Text.literal("gui.mtrsteamloco.config.client.shaderactive")
            ).build());
        }
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.rail3d"),
                        ClientConfig.enableRail3D
                ).setSaveConsumer(checked -> {
                    ClientConfig.enableRail3D = checked;
                    Minecraft.getInstance().levelRenderer.allChanged();
                }).setDefaultValue(true).build()
        );
        common.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("gui.mtrsteamloco.config.client.rail3d.description")
                ).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.preloadbbmodel"),
                        ClientConfig.enableBbModelPreload
                ).setSaveConsumer(checked -> {
                    ClientConfig.enableBbModelPreload = checked;
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().reloadResourcePacks());
                }).setDefaultValue(false).build()
        );
        common.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("gui.mtrsteamloco.config.client.preloadbbmodel.description")
                ).build()
        );

        ConfigCategory misc = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.misc")
        );
        misc.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.shadercompat"),
                        ClientConfig.disableOptimization
                ).setTooltip(
                        Text.translatable("gui.mtrsteamloco.config.client.shadercompat.description")
                ).setSaveConsumer(checked -> ClientConfig.disableOptimization = checked).setDefaultValue(false).build()
        );
        misc.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("gui.mtrsteamloco.config.client.railrender.description")
                ).build()
        );
        misc.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.trainrender"),
                        ClientConfig.enableTrainRender
                ).setSaveConsumer(checked -> ClientConfig.enableTrainRender = checked).setDefaultValue(true).build()
        );
        misc.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.railrender"),
                        ClientConfig.enableRailRender
                ).setSaveConsumer(checked -> ClientConfig.enableRailRender = checked).setDefaultValue(true).build()
        );
        misc.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.slsmoke"),
                        ClientConfig.enableSmoke
                ).setSaveConsumer(checked -> ClientConfig.enableSmoke = checked).setDefaultValue(true).build()
        );
        return builder.build();
    }
}