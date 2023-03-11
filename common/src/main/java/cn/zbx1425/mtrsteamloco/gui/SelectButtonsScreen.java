package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.ScreenMapper;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public abstract class SelectButtonsScreen extends ScreenMapper {

    protected final int SQUARE_SIZE = 20;
    protected final int TEXT_HEIGHT = 8;
    protected final int COLUMN_WIDTH = 80;

    private int page = 0;

    private final Button btnPrevPage = UtilitiesClient.newButton(Text.literal("↑"), sender -> {
        page--; loadPage();
    });
    private final Button btnNextPage = UtilitiesClient.newButton(Text.literal("↓"), sender -> {
        page++; loadPage();
    });

    private final List<List<Button>> pages = new ArrayList<>();
    private final HashMap<Button, String> btnKeys = new HashMap<>();

    protected SelectButtonsScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {

        List<Pair<String, String>> entries = getRegistryEntries().stream()
                .sorted((a, b) -> Integer.compare(b.getSecond().length(), a.getSecond().length()))
                .toList();

        int pageRows = (height - SQUARE_SIZE * 4 - TEXT_HEIGHT * 2) / (SQUARE_SIZE);
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
        btnKeys.clear();
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
                Button btnToPlace = UtilitiesClient.newButton(
                        Text.literal(btnText),
                        (sender) -> {
                            onBtnClick(btnKey);
                            loadPage();
                        }
                );
                IDrawing.setPositionAndWidth(
                        btnToPlace,
                        crntCol * COLUMN_WIDTH + SQUARE_SIZE, crntRow * SQUARE_SIZE + SQUARE_SIZE * 3,
                        colSpan * COLUMN_WIDTH
                );
                pages.get(crntPage).add(btnToPlace);
                btnKeys.put(btnToPlace, btnKey);
                entries.remove(i);
                crntCol += colSpan;
                break;
            }
            if (!btnPlaced) {
                crntRow++;
                crntCol = 0;
            }
        }

        IDrawing.setPositionAndWidth(btnPrevPage, width - SQUARE_SIZE * 2, SQUARE_SIZE, SQUARE_SIZE);
        IDrawing.setPositionAndWidth(btnNextPage, width - SQUARE_SIZE * 2, SQUARE_SIZE * 5, SQUARE_SIZE);

        super.init();
    }

    protected abstract void loadPage();

    protected abstract void onBtnClick(String btnKey);

    protected abstract List<Pair<String, String>> getRegistryEntries();

    protected void renderSelectPage(PoseStack poseStack) {
        drawCenteredString(poseStack, font, Integer.toString(page + 1), (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, "/", (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        drawCenteredString(poseStack, font, Integer.toString(pages.size()), (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
    }

    protected void loadSelectPage(Function<String, Boolean> btnActivePredicate) {
        addRenderableWidget(btnPrevPage);
        addRenderableWidget(btnNextPage);
        btnPrevPage.active = page != 0;
        btnNextPage.active = page != this.pages.size() - 1;

        for (Button button : pages.get(page)) {
            addRenderableWidget(button);
        }
        for (Button button : pages.get(page)) {
            button.active = btnActivePredicate.apply(btnKeys.get(button));
        }

        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, height - SQUARE_SIZE * 2, SQUARE_SIZE);
    }
}
