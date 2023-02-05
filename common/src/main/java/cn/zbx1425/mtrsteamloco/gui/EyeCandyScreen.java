package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.ScreenMapper;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class EyeCandyScreen extends ScreenMapper {

    final int SQUARE_SIZE = 20;
    final int TEXT_HEIGHT = 8;
    final int COLUMN_WIDTH = 80;

    private boolean isSelectingModel = false;
    private int page = 0;

    private final Button btnPrevPage = UtilitiesClient.newButton(Text.literal("↑"), sender -> {
        page--; loadPage();
    });
    private final Button btnNextPage = UtilitiesClient.newButton(Text.literal("↓"), sender -> {
        page++; loadPage();
    });

    private static final String INSTRUCTION_LINK = "https://www.zbx1425.cn/nautilus/mtr-nte/#/eyecandy";
    private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, TEXT_HEIGHT, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
        this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(INSTRUCTION_LINK);
            }
            this.minecraft.setScreen(this);
        }, INSTRUCTION_LINK, true));
    });

    private final List<List<Button>> pages = new ArrayList<>();
    private final HashMap<Button, String> btnKeys = new HashMap<>();

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
                .sorted((a, b) -> Integer.compare(b.getSecond().length(), a.getSecond().length()))
                .toList());

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
                            updateBlockEntity((blockEntity) -> blockEntity.prefabId = btnKey);
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
        IDrawing.setPositionAndWidth(lblInstruction, SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, width - SQUARE_SIZE * 4);

        loadPage();
    }

    @Override
    public void render(@NotNull PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        super.render(poseStack, i, j, f);

        if (isSelectingModel) {
            drawCenteredString(poseStack, font, Integer.toString(page + 1), (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 2.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
            drawCenteredString(poseStack, font, "/", (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 3.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
            drawCenteredString(poseStack, font, Integer.toString(pages.size()), (int) (width - SQUARE_SIZE * 1.5F), (int) (SQUARE_SIZE * 4.5F - TEXT_HEIGHT * 0.5F), 0xFFFFFFFF);
        }
    }

    private void loadPage() {
        clearWidgets();

        Optional<BlockEyeCandy.BlockEntityEyeCandy> optionalBlockEntity = getBlockEntity();
        if (optionalBlockEntity.isEmpty()) { this.onClose(); return; }
        BlockEyeCandy.BlockEntityEyeCandy blockEntity = optionalBlockEntity.get();

        if (isSelectingModel) {
            loadModelSelectPage(blockEntity);
        } else {
            loadMainPage(blockEntity);
        }
    }

    private void loadMainPage(BlockEyeCandy.BlockEntityEyeCandy blockEntity) {
        EyeCandyProperties properties = EyeCandyRegistry.elements.get(blockEntity.prefabId);
        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                properties != null ? properties.name : Text.literal("???"),
                sender -> { isSelectingModel = true; loadPage(); }
        )), SQUARE_SIZE, SQUARE_SIZE, COLUMN_WIDTH * 3);

        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateX * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateX = (value - 20) * 5f / 100f); return "TX " + ((value - 20) * 5) + "cm"; }
        )), SQUARE_SIZE, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateX * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateY = (value - 20) * 5f / 100f); return "TY " + ((value - 20) * 5) + "cm"; }
        )), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateX * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateZ = (value - 20) * 5f / 100f); return "TZ " + ((value - 20) * 5) + "cm"; }
        )), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3 * 2, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);

        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateX) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateX = (float)Math.toRadians((value - 18) * 5f)); return "RX " + ((value - 18) * 5) + "°"; }
        )), SQUARE_SIZE, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateY) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateY = (float)Math.toRadians((value - 18) * 5f)); return "RY " + ((value - 18) * 5) + "°"; }
        )), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                18 * 2, (int)Math.round(Math.toDegrees(blockEntity.rotateZ) / 5f) + 18,
                value -> { updateBlockEntity(be -> be.rotateZ = (float)Math.toRadians((value - 18) * 5f)); return "RZ " + ((value - 18) * 5) + "°"; }
        )), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3 * 2, SQUARE_SIZE * 4, (width - SQUARE_SIZE * 2) / 3);

        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 6, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.eye_candy.full_light"),
                checked -> updateBlockEntity((be) -> be.fullLight = checked)
        )).setChecked(blockEntity.fullLight);

        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, height - SQUARE_SIZE * 2, SQUARE_SIZE);
    }

    private void loadModelSelectPage(BlockEyeCandy.BlockEntityEyeCandy blockEntity) {
        addRenderableWidget(btnPrevPage);
        addRenderableWidget(btnNextPage);
        btnPrevPage.active = page != 0;
        btnNextPage.active = page != this.pages.size() - 1;

        for (Button button : pages.get(page)) {
            addRenderableWidget(button);
        }
        for (Button button : pages.get(page)) {
            button.active = !btnKeys.get(button).equals(blockEntity.prefabId);
        }

        addRenderableWidget(lblInstruction);
        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                Text.literal("X"), sender -> this.onClose()
        )), width - SQUARE_SIZE * 2, height - SQUARE_SIZE * 2, SQUARE_SIZE);
    }

    private void updateBlockEntity(Consumer<BlockEyeCandy.BlockEntityEyeCandy> modifier) {
        getBlockEntity().ifPresent(blockEntity -> {
            modifier.accept(blockEntity);
            PacketUpdateBlockEntity.sendUpdateC2S(blockEntity);
        });
    }

    private Optional<BlockEyeCandy.BlockEntityEyeCandy> getBlockEntity() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return Optional.empty();
        return level.getBlockEntity(editingBlockPos, Main.BLOCK_ENTITY_TYPE_EYE_CANDY.get());
    }

    @Override
    public void onClose() {
        if (isSelectingModel) {
            isSelectingModel = false;
            loadPage();
        } else {
            this.minecraft.setScreen(null);
        }
    }
}
