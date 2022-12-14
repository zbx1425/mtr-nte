package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EyeCandyScreen extends Screen {

    final int SQUARE_SIZE = 20;
    final int TEXT_HEIGHT = 8;
    final int COLUMN_WIDTH = 80;

    private int page = 0;

    private final Button btnPrevPage = new Button(0, SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, Text.literal("↑"), sender -> {
        page--;
        loadPage();
    });
    private final Button btnNextPage = new Button(0, SQUARE_SIZE * 5, SQUARE_SIZE, SQUARE_SIZE, Text.literal("↓"), sender -> {
        page++;
        loadPage();
    });
    private final Button btnClose = new Button(0, SQUARE_SIZE * 7, SQUARE_SIZE, SQUARE_SIZE, Text.literal("X"), sender -> {
        onClose();
    });

    private List<List<Button>> pages = new ArrayList<>();

    private final BlockPos editingBlockPos;

    public EyeCandyScreen(BlockPos blockPos) {
        super(Text.literal("Select EyeCandy"));
        this.editingBlockPos = blockPos;
    }


    @Override
    protected void init() {
        super.init();

        List<Pair<String, String>> entries = new ArrayList<>(
                EyeCandyRegistry.elements.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().name.getString()))
                .sorted(Comparator.comparingInt(a -> a.getSecond().length()))
                .toList());

        int pageRows = (height - SQUARE_SIZE * 2) / (SQUARE_SIZE);
        int pageCols = (width - SQUARE_SIZE * 4) / (COLUMN_WIDTH);

        for (int i = 0; i < entries.size(); i++) {
            String btnText = entries.get(i).getSecond();
            int colSpan = (int)Math.ceil((font.width(btnText) + SQUARE_SIZE) * 1f / COLUMN_WIDTH);
            while (colSpan > pageCols && btnText.length() > 0) {
                btnText = btnText.substring(0, btnText.length() - 2);
                colSpan = (int)Math.ceil((font.width(btnText) + SQUARE_SIZE) * 1f / COLUMN_WIDTH);
            }
            entries.set(i, new Pair<>(entries.get(i).getFirst(), btnText));
        }

        pages.clear();
        pages.add(new ArrayList<>());
        int crntPage = 0;
        int crntRow = 0;
        int crntCol = 0;
        while (entries.size() > 0) {
            if (crntCol >= pageCols) {
                crntRow++;
                crntCol = 0;
            }
            if (crntRow >= pageRows) {
                pages.add(new ArrayList<>());
                crntPage++;
                crntRow = 0;
                crntCol = 0;
            }
            boolean btnPlaced = false;
            for (int i = 0; i < entries.size(); i++) {
                String btnKey = entries.get(i).getFirst();
                String btnText = entries.get(i).getSecond();
                int colSpan = (int)Math.ceil((font.width(btnText) + SQUARE_SIZE) * 1f / COLUMN_WIDTH);
                if (crntCol + colSpan > pageCols) continue;
                btnPlaced = true;
                pages.get(crntPage).add(new Button(
                        crntCol * COLUMN_WIDTH + SQUARE_SIZE, crntRow * SQUARE_SIZE + SQUARE_SIZE,
                        colSpan * COLUMN_WIDTH, SQUARE_SIZE,
                        Text.literal(btnText),
                        (sender) -> onBtnClicked(btnKey)
                ));
                entries.remove(i);
                crntCol += colSpan;
                break;
            }
            if (!btnPlaced) {
                crntRow++;
                crntCol = 0;
            }
        }

        btnPrevPage.x = width - SQUARE_SIZE * 2;
        btnNextPage.x = width - SQUARE_SIZE * 2;
        btnClose.x = width - SQUARE_SIZE * 2;
        btnClose.y = height - SQUARE_SIZE * 2;
        loadPage();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);

        drawCenteredString(poseStack, font, Integer.toString(page + 1), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, "/", (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, Integer.toString(pages.size()), (int)(width - SQUARE_SIZE * 1.5F), (int)(SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void loadPage() {
        clearWidgets();

        addRenderableWidget(btnPrevPage);
        addRenderableWidget(btnNextPage);
        addRenderableWidget(btnClose);
        btnPrevPage.active = page != 0;
        btnNextPage.active = page != this.pages.size() - 1;

        for (Button button : pages.get(page)) {
            addRenderableWidget(button);
        }
    }

    private void onBtnClicked(String key) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        level.getBlockEntity(editingBlockPos, Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get()).ifPresent(blockEntity -> {
            blockEntity.prefabId = key;
            PacketUpdateBlockEntity.sendUpdateC2S(blockEntity);
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
