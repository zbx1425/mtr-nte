package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.EyeCandyProperties;
import cn.zbx1425.mtrsteamloco.data.EyeCandyRegistry;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class EyeCandyScreen extends SelectListScreen {

    private boolean isSelectingModel = false;

    private static final String INSTRUCTION_LINK = "https://www.zbx1425.cn/nautilus/mtr-nte/#/eyecandy";
    private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
        this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(INSTRUCTION_LINK);
            }
            this.minecraft.setScreen(this);
        }, INSTRUCTION_LINK, true));
    });

    private final BlockPos editingBlockPos;

    public EyeCandyScreen(BlockPos blockPos) {
        super(Text.literal("Select EyeCandy"));
        this.editingBlockPos = blockPos;
    }

    @Override
    protected void init() {
        super.init();

        loadPage();
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
#else
    public void render(@NotNull PoseStack guiGraphics, int i, int j, float f) {
#endif
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, i, j, f);

        if (isSelectingModel) {
            super.renderSelectPage(guiGraphics);
        }
    }

    @Override
    protected void loadPage() {
        clearWidgets();

        Optional<BlockEyeCandy.BlockEntityEyeCandy> optionalBlockEntity = getBlockEntity();
        if (optionalBlockEntity.isEmpty()) { this.onClose(); return; }
        BlockEyeCandy.BlockEntityEyeCandy blockEntity = optionalBlockEntity.get();

        if (isSelectingModel) {
            scrollList.visible = true;
            loadSelectPage(key -> !key.equals(blockEntity.prefabId));
            lblInstruction.alignR = true;
            IDrawing.setPositionAndWidth(lblInstruction, width / 2 + SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, 0);
            lblInstruction.setWidth(width / 2 - SQUARE_SIZE * 2);
            addRenderableWidget(lblInstruction);
        } else {
            scrollList.visible = false;
            loadMainPage(blockEntity);
        }
    }

    @Override
    protected void onBtnClick(String btnKey) {
        updateBlockEntity((blockEntity) -> blockEntity.prefabId = btnKey);
    }

    @Override
    protected List<Pair<String, String>> getRegistryEntries() {
        return EyeCandyRegistry.elements.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().name.getString()))
                .toList();
    }

    private void loadMainPage(BlockEyeCandy.BlockEntityEyeCandy blockEntity) {
        EyeCandyProperties properties = EyeCandyRegistry.elements.get(blockEntity.prefabId);
        IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                properties != null ? properties.name : Text.literal(blockEntity.prefabId + " (???)"),
                sender -> { isSelectingModel = true; loadPage(); }
        )), SQUARE_SIZE, SQUARE_SIZE, COLUMN_WIDTH * 3);

        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateX * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateX = (value - 20) * 5f / 100f); return "TX " + ((value - 20) * 5) + "cm"; }
        )), SQUARE_SIZE, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateY * 100 / 5f) + 20,
                value -> { updateBlockEntity(be -> be.translateY = (value - 20) * 5f / 100f); return "TY " + ((value - 20) * 5) + "cm"; }
        )), SQUARE_SIZE + (width - SQUARE_SIZE * 2) / 3, SQUARE_SIZE * 3, (width - SQUARE_SIZE * 2) / 3);
        IDrawing.setPositionAndWidth(addRenderableWidget(new WidgetSlider(
                20 * 2, (int)Math.round(blockEntity.translateZ * 100 / 5f) + 20,
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
