package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.ScreenMapper;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterTextField;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;

public abstract class SelectListScreen extends ScreenMapper {

    protected final int SQUARE_SIZE = 20;
    protected final int TEXT_HEIGHT = 8;
    protected final int COLUMN_WIDTH = 80;

    protected WidgetScrollList scrollList = new WidgetScrollList(0, 21, width / 2, height + 2);

    private final HashMap<Button, String> btnKeys = new HashMap<>();

    private final WidgetBetterTextField textFieldSearch = new WidgetBetterTextField(Text.translatable("gui.mtr.search").getString());

    protected SelectListScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

        scrollList.setWidth(width / 2);
        scrollList.setHeight(height - SQUARE_SIZE + 2);

        IDrawing.setPositionAndWidth(textFieldSearch, 1, 1, width / 2 - 2);
        textFieldSearch.setResponder(changed -> updateEntries());
        textFieldSearch.moveCursorToStart();

        updateEntries();
    }

    void updateEntries() {
        List<Pair<String, String>> entries = new ArrayList<>(getRegistryEntries().stream()
                .sorted(Comparator.comparing(Pair::getSecond))
                .toList());

        btnKeys.clear();
        scrollList.children.clear();
        int buttonsPlaced = 0;
        for (int i = 0; i < entries.size(); i++) {
            String btnKey = entries.get(i).getFirst();
            String btnText = entries.get(i).getSecond();
            if (!textFieldSearch.getValue().isEmpty() && !btnText.contains(textFieldSearch.getValue())) {
                continue;
            }
            Button btnToPlace = UtilitiesClient.newButton(
                    Text.literal(btnText),
                    (sender) -> {
                        onBtnClick(btnKey);
                        Minecraft.getInstance().tell(this::loadPage);
                    }
            );
            IDrawing.setPositionAndWidth(
                    btnToPlace,
                    0, buttonsPlaced * SQUARE_SIZE,
                    scrollList.getWidth()
            );
            scrollList.children.add(btnToPlace);
            btnKeys.put(btnToPlace, btnKey);
            buttonsPlaced++;
        }
    }

    @Override
    public void tick() {
        textFieldSearch.tick();
        super.tick();
    }

    protected abstract void loadPage();

    protected abstract void onBtnClick(String btnKey);

    protected abstract List<Pair<String, String>> getRegistryEntries();

#if MC_VERSION >= "12000"
    protected void renderSelectPage(GuiGraphics guiGraphics) {
#else
    protected void renderSelectPage(PoseStack guiGraphics) {
#endif

    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        scrollList.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    protected void loadSelectPage(Function<String, Boolean> btnActivePredicate) {
        addRenderableWidget(scrollList);
        for (AbstractWidget button : scrollList.children) {
            button.active = btnActivePredicate.apply(btnKeys.get((Button)button));
        }
        addRenderableWidget(textFieldSearch);

        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, SQUARE_SIZE, SQUARE_SIZE);
    }

    @Override
#if MC_VERSION >= "12000"
    public void renderBackground(GuiGraphics guiGraphics) {
#else
    public void renderBackground(PoseStack guiGraphics) {
#endif
        if (scrollList.visible) return;
        super.renderBackground(guiGraphics);
    }

    public boolean isSelecting() {
        return scrollList.visible;
    }

}
