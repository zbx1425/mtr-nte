package cn.zbx1425.mtrsteamloco.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import mtr.mappings.Text;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;

public class ErrorScreen {

    public static Screen createScreen(List<String> errorList, Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("gui.mtrsteamloco.error.title"))
                .solidBackground()
                .setDoesConfirmSave(false);
        ConfigCategory category = builder.getOrCreateCategory(Text.literal(""));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        category.addEntry(entryBuilder
                .startTextDescription(Text.translatable("gui.mtrsteamloco.error.explain"))
                .build()
        );
        for (String error : errorList) {
            category.addEntry(entryBuilder
                    .startTextDescription(Text.literal(error))
                    .build()
            );
        }
        return builder.build();
    }
}
