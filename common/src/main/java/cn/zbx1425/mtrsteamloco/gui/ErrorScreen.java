package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
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

    private final Button btnPrevPage = new Button(0, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, Text.literal("↑"), sender -> {
        changeOffset(-1);
    });
    private final Button btnNextPage = new Button(0, SQUARE_SIZE * 5, SQUARE_SIZE, SQUARE_SIZE, Text.literal("↓"), sender -> {
        changeOffset(1);
    });
    private final Button btnClose = new Button(0, SQUARE_SIZE * 7, SQUARE_SIZE, SQUARE_SIZE, Text.literal("X"), sender -> {
        Minecraft.getInstance().setScreen(this.parentScreen);
    });

    @Override
    protected void init() {
        super.init();
        btnPrevPage.x = width - SQUARE_SIZE * 2;
        btnNextPage.x = width - SQUARE_SIZE * 2;
        btnClose.x = width - SQUARE_SIZE * 2;
        btnClose.y = height - SQUARE_SIZE * 2;
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.fillGradient(poseStack, 0, 0, this.width, this.height, 0xFF03458C, 0xFF001A3B);
        super.render(poseStack, i, j, f);

        drawCenteredString(poseStack, font, Integer.toString(offset + 1), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, "/", (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, Integer.toString(pages), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);

        for (int n = offset * pageLines; n < Math.min(splitErrorList.length, (offset + 1) * pageLines); n++) {
            drawString(poseStack, font, splitErrorList[n], SQUARE_SIZE, (n - offset * pageLines) * (TEXT_HEIGHT + 1) + SQUARE_SIZE, 0xFFFFFFFF);
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
