package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.CustomResources;
import cn.zbx1425.mtrsteamloco.render.ShadersModHandler;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
#endif
import net.minecraft.client.gui.screens.Screen;

public final class ConfigScreen {

    public static Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("gui.mtrsteamloco.config.client.title"))
                .setDoesConfirmSave(false)
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
                    boolean needReload = ClientConfig.enableRail3D != checked;
                    ClientConfig.enableRail3D = checked;
                    if (needReload) {
                        Minecraft.getInstance().levelRenderer.allChanged();
                    }
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
                    boolean needReload = ClientConfig.enableBbModelPreload != checked;
                    ClientConfig.enableBbModelPreload = checked;
                    if (needReload) {
                        Minecraft.getInstance().execute(() -> Minecraft.getInstance().reloadResourcePacks());
                    }
                }).setDefaultValue(false).build()
        );
        common.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("gui.mtrsteamloco.config.client.preloadbbmodel.description")
                ).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.scriptdebugoverlay"),
                        ClientConfig.enableScriptDebugOverlay
                ).setSaveConsumer(checked -> ClientConfig.enableScriptDebugOverlay = checked).setDefaultValue(false).build()
        );

        /*
        ConfigCategory misc = builder.getOrCreateCategory(
                Text.translatable("gui.mtrsteamloco.config.client.category.misc")
        );
         */
        common.addEntry(entryBuilder.startTextDescription(
                        Text.translatable("gui.mtrsteamloco.config.client.category.misc")
                ).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.translucentsort"),
                        ClientConfig.translucentSort
                ).setTooltip(
                        Text.translatable("gui.mtrsteamloco.config.client.translucentsort.description")
                ).setSaveConsumer(checked -> ClientConfig.translucentSort = checked).setDefaultValue(false).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.shadercompat"),
                        ClientConfig.enableOptimization
                ).setSaveConsumer(checked -> ClientConfig.enableOptimization = checked).setDefaultValue(true).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.trainrender"),
                        ClientConfig.enableTrainRender
                ).setSaveConsumer(checked -> {
                    ClientConfig.enableTrainRender = checked;
                    CustomResources.resetComponents();
                }).setDefaultValue(true).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.trainsound"),
                        ClientConfig.enableTrainSound
                ).setSaveConsumer(checked -> {
                    ClientConfig.enableTrainSound = checked;
                    CustomResources.resetComponents();
                }).setDefaultValue(true).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.railrender"),
                        ClientConfig.enableRailRender
                ).setSaveConsumer(checked -> ClientConfig.enableRailRender = checked).setDefaultValue(true).build()
        );
        common.addEntry(entryBuilder
                .startBooleanToggle(
                        Text.translatable("gui.mtrsteamloco.config.client.slsmoke"),
                        ClientConfig.enableSmoke
                ).setSaveConsumer(checked -> ClientConfig.enableSmoke = checked).setDefaultValue(true).build()
        );

        builder.setSavingRunnable(ClientConfig::save);
        return builder.build();
    }
}