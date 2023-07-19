package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.client.IDrawing;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.Arrays;
import java.util.List;

public class ErrorScreen extends Screen {

    private final List<String> errorList;

    private String[] splitErrorList;

    private Screen parentScreen;

    public ErrorScreen(List<String> errorList, Screen parentScreen) {
        super(Text.literal("Error"));
        this.errorList = errorList;
        this.parentScreen = parentScreen;
    }

    final int SQUARE_SIZE = 20;
    final int TEXT_HEIGHT = 8;

    private int offset = 0;
    private int pageLines;
    private int pages;

    private final Button btnPrevPage = UtilitiesClient.newButton(Text.literal("↑"), sender -> changeOffset(-1));
    private final Button btnNextPage = UtilitiesClient.newButton(Text.literal("↓"), sender -> changeOffset(1));
    private final Button btnClose = UtilitiesClient.newButton(Text.literal("X"), sender -> this.onClose());

    @Override
    protected void init() {
        super.init();
        IDrawing.setPositionAndWidth(btnPrevPage, width - SQUARE_SIZE * 2, SQUARE_SIZE, SQUARE_SIZE);
        IDrawing.setPositionAndWidth(btnNextPage, width - SQUARE_SIZE * 2, SQUARE_SIZE * 5, SQUARE_SIZE);
        IDrawing.setPositionAndWidth(btnClose, width - SQUARE_SIZE * 2, height - SQUARE_SIZE * 2, SQUARE_SIZE);
        addRenderableWidget(btnPrevPage);
        addRenderableWidget(btnNextPage);
        addRenderableWidget(btnClose);
        splitErrorList = font.getSplitter()
                .splitLines(Text.literal("NTE Resource Loading Exception Report\n\n" + String.join("\n",
                    errorList.stream().flatMap(l -> Arrays.stream(l.split("\n"))).filter(l ->
                        !(l.contains("CompletableFuture") || l.contains("SimpleReloadInstance") || l.contains("BlockableEventLoop")))
                            .map(l -> l.replace("\t", "  ").replace("\r", "")).toList()
                )), width - SQUARE_SIZE * 4, Style.EMPTY)
                .stream().map(FormattedText::getString).toArray(String[]::new);
        pageLines = (height - SQUARE_SIZE * 2) / (TEXT_HEIGHT + 1);
        pages = (int) Math.ceil(splitErrorList.length * 1.0F / pageLines);
        changeOffset(0);
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
#else
    public void render(PoseStack guiGraphics, int i, int j, float f) {
#endif
#if MC_VERSION >= "12000"
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xFF03458C, 0xFF001A3B);
#else
        fillGradient(guiGraphics, 0, 0, this.width, this.height, 0xFF03458C, 0xFF001A3B);
#endif
        super.render(guiGraphics, i, j, f);

#if MC_VERSION >= "12000"
        guiGraphics.drawCenteredString(font, Integer.toString(offset + 1), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        guiGraphics.drawCenteredString(font, "/", (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        guiGraphics.drawCenteredString(font, Integer.toString(pages), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
#else
        drawCenteredString(guiGraphics, font, Integer.toString(offset + 1), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(guiGraphics, font, "/", (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(guiGraphics, font, Integer.toString(pages), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
#endif

        for (int n = offset * pageLines; n < Math.min(splitErrorList.length, (offset + 1) * pageLines); n++) {
#if MC_VERSION >= "12000"
            guiGraphics.drawString(font, splitErrorList[n], SQUARE_SIZE, (n - offset * pageLines) * (TEXT_HEIGHT + 1) + SQUARE_SIZE, 0xFFFFFFFF);
#else
            drawString(guiGraphics, font, splitErrorList[n], SQUARE_SIZE, (n - offset * pageLines) * (TEXT_HEIGHT + 1) + SQUARE_SIZE, 0xFFFFFFFF);
#endif
        }
    }

    private void changeOffset(int pages) {
        offset += pages;
        offset = Math.max(0, Math.min(offset, this.pages - 1));
        btnPrevPage.active = offset != 0;
        btnNextPage.active = offset != this.pages - 1;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parentScreen);
    }
}
