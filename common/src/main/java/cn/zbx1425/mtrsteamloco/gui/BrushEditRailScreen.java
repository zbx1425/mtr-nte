package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import cn.zbx1425.mtrsteamloco.data.*;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateHoldingItem;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateRail;
import cn.zbx1425.mtrsteamloco.render.RailPicker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class BrushEditRailScreen extends SelectButtonsScreen {

    private boolean isSelectingModel = false;

    public BrushEditRailScreen() {
        super(Text.literal("Select rail arguments"));
    }

    @Override
    protected void init() {
        super.init();

        loadPage();
    }

    @Override
    protected void loadPage() {
        clearWidgets();

        if (isSelectingModel) {
            CompoundTag brushTag = getBrushTag();
            String modelKey = brushTag == null ? "" : brushTag.getString("ModelKey");
            loadSelectPage(key -> !key.equals(modelKey));
        } else {
            loadMainPage();
        }
    }

    @Override
    protected void onBtnClick(String btnKey) {
        updateBrushTag(compoundTag -> compoundTag.putString("ModelKey", btnKey));
    }

    private void loadMainPage() {
        CompoundTag brushTag = getBrushTag();
        boolean enableModelKey = brushTag != null && brushTag.contains("ModelKey");
        String modelKey = brushTag == null ? "" : brushTag.getString("ModelKey");

        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_model_key"),
                checked -> updateBrushTag(compoundTag -> {
                    if (checked) {
                        compoundTag.putString("ModelKey", "");
                    } else {
                        compoundTag.remove("ModelKey");
                    }
                })
        )).setChecked(enableModelKey);
        if (enableModelKey) {
            RailModelProperties properties = RailModelRegistry.elements.get(modelKey);
            IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                    properties != null ? properties.name : Text.literal(modelKey + " (???)"),
                    sender -> {
                        isSelectingModel = true;
                        loadPage();
                    }
            )), SQUARE_SIZE, SQUARE_SIZE * 2, COLUMN_WIDTH * 3);
        }
    }

    @Override
    protected List<Pair<String, String>> getRegistryEntries() {
        return RailModelRegistry.elements.entrySet().stream()
                .map(e -> new Pair<>(e.getKey(), e.getValue().name.getString()))
                .toList();
    }

    private CompoundTag getBrushTag() {
        if (Minecraft.getInstance().player == null) return null;
        ItemStack brushItem = Minecraft.getInstance().player.getMainHandItem();
        if (!brushItem.is(mtr.Items.BRUSH.get())) return null;
        CompoundTag nteTag = brushItem.getTagElement("NTERailBrush");
        return nteTag;
    }

    private void updateBrushTag(Consumer<CompoundTag> modifier) {
        if (Minecraft.getInstance().player == null) return;
        ItemStack brushItem = Minecraft.getInstance().player.getMainHandItem();
        if (!brushItem.is(mtr.Items.BRUSH.get())) return;
        CompoundTag nteTag = brushItem.getOrCreateTagElement("NTERailBrush");
        modifier.accept(nteTag);
        loadPage();
        applyBrushToPickedRail(nteTag);
        PacketUpdateHoldingItem.sendUpdateC2S();
    }

    public static boolean applyBrushToPickedRail(CompoundTag railBrushProp) {
        if (railBrushProp == null) return false;
        if (RailPicker.pickedRail == null) return false;
        if (railBrushProp.contains("ModelKey")) {
            ((RailExtraSupplier) RailPicker.pickedRail).setModelKey(railBrushProp.getString("ModelKey"));
            PacketUpdateRail.sendUpdateC2S(RailPicker.pickedRail, RailPicker.pickedPosStart, RailPicker.pickedPosEnd);
        }
        return true;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);

        if (isSelectingModel) {
            renderSelectPage(poseStack);
        }
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
