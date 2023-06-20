package cn.zbx1425.mtrsteamloco.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.ScreenMapper;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetSilentImageButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;

public abstract class SelectButtonsScreen extends ScreenMapper {

    protected final int SQUARE_SIZE = 20;
    protected final int TEXT_HEIGHT = 8;
    protected final int COLUMN_WIDTH = 80;

    protected WidgetScrollList scrollList = null;

    private final HashMap<Button, String> btnKeys = new HashMap<>();

    protected SelectButtonsScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        List<Pair<String, String>> entries = new ArrayList<>(getRegistryEntries().stream()
                .sorted(Comparator.comparing(Pair::getFirst))
                .toList());

        scrollList = new WidgetScrollList(0, -1, width / 2, height + 2);

        for (int i = 0; i < entries.size(); i++) {
            String btnText = entries.get(i).getSecond();
            int textWidth = font.width(btnText) + SQUARE_SIZE;
            while (textWidth > scrollList.getWidth() && btnText.length() > 0) {
                btnText = btnText.substring(0, btnText.length() - 2);
                textWidth = font.width(btnText) + SQUARE_SIZE;
            }
            entries.set(i, new Pair<>(entries.get(i).getFirst(), btnText));
        }

        btnKeys.clear();
        for (int i = 0; i < entries.size(); i++) {
            String btnKey = entries.get(i).getFirst();
            String btnText = entries.get(i).getSecond();
            Button btnToPlace = UtilitiesClient.newButton(
                    Text.literal(btnText),
                    (sender) -> {
                        onBtnClick(btnKey);
                        Minecraft.getInstance().tell(this::loadPage);
                    }
            );
            IDrawing.setPositionAndWidth(
                    btnToPlace,
                    0, i * SQUARE_SIZE,
                    scrollList.getWidth()
            );
            scrollList.children.add(btnToPlace);
            btnKeys.put(btnToPlace, btnKey);
        }
        super.init();
    }

    protected abstract void loadPage();

    protected abstract void onBtnClick(String btnKey);

    protected abstract List<Pair<String, String>> getRegistryEntries();

    protected void renderSelectPage(PoseStack poseStack) {

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

        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, SQUARE_SIZE, SQUARE_SIZE);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int vOffset) {
        if (scrollList.visible) return;
        super.renderBackground(poseStack, vOffset);
    }

    public boolean isSelecting() {
        return scrollList.visible;
    }

}
