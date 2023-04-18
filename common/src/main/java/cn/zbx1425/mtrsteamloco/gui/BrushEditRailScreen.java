package cn.zbx1425.mtrsteamloco.gui;

import cn.zbx1425.mtrsteamloco.data.*;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateHoldingItem;
import cn.zbx1425.mtrsteamloco.network.PacketUpdateRail;
import cn.zbx1425.mtrsteamloco.render.RailPicker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mtr.client.IDrawing;
import mtr.data.Rail;
import mtr.mappings.Text;
import mtr.mappings.UtilitiesClient;
import mtr.screen.WidgetBetterCheckbox;
import mtr.screen.WidgetBetterTextField;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
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
        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 2, COLUMN_WIDTH * 2, SQUARE_SIZE,
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
            )), SQUARE_SIZE, SQUARE_SIZE * 3, COLUMN_WIDTH * 3);
        }

        boolean enableVertCurveRadius = brushTag != null && brushTag.contains("VerticalCurveRadius");
        float vertCurveRadius = brushTag == null ? 0f : brushTag.getFloat("VerticalCurveRadius");
        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 5, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_vertical_curve_radius"),
                checked -> updateBrushTag(compoundTag -> {
                    if (checked) {
                        compoundTag.putFloat("VerticalCurveRadius", 0);
                    } else {
                        compoundTag.remove("VerticalCurveRadius");
                    }
                })
        )).setChecked(enableVertCurveRadius);
        if (enableVertCurveRadius) {
            WidgetBetterTextField radiusInput = new WidgetBetterTextField("^(?:[1-9]\\d*|0)?(?:\\.\\d+)?$",
                    "0", 5);
            WidgetLabel valuesLabel = new WidgetLabel(SQUARE_SIZE, SQUARE_SIZE * 7 + 10, width - SQUARE_SIZE * 2,
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_irl_ref"));
            valuesLabel.setMessage(Text.literal(getVerticalValueText(vertCurveRadius)));
            radiusInput.setValue(Float.toString(vertCurveRadius));
            radiusInput.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        float newRadius = Float.parseFloat(text);
                        valuesLabel.setMessage(Text.literal(getVerticalValueText(newRadius)));
                        updateBrushTag(compoundTag -> {
                            compoundTag.putFloat("VerticalCurveRadius", newRadius);
                        });
                    } catch (Exception ignored) { }
                }
            });
            IDrawing.setPositionAndWidth(addRenderableWidget(radiusInput), SQUARE_SIZE, SQUARE_SIZE * 6, COLUMN_WIDTH * 2);
            IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_max"),
                    sender -> radiusInput.setValue("0")
            )), SQUARE_SIZE + COLUMN_WIDTH * 2, SQUARE_SIZE * 6, COLUMN_WIDTH);
            IDrawing.setPositionAndWidth(addRenderableWidget(UtilitiesClient.newButton(
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_none"),
                    sender -> radiusInput.setValue("-1")
            )), SQUARE_SIZE + COLUMN_WIDTH * 3, SQUARE_SIZE * 6, COLUMN_WIDTH);
            addRenderableWidget(new WidgetLabel(SQUARE_SIZE, SQUARE_SIZE * 7, width - SQUARE_SIZE * 2,
                    Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_irl_ref")));
            addRenderableWidget(valuesLabel);
        }
    }

    private String getVerticalValueText(float verticalRadius) {
        Rail rail = RailPicker.pickedRail;
        if (rail == null) return "(???)";
        double maxRadius = Mth.length(((RailExtraSupplier)rail).getHeight() / 2f, rail.getLength() / 2);
        double vTheta = RailExtraSupplier.getVTheta(rail, verticalRadius);
        return Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_values",
                String.format("%.1f", maxRadius), String.format("%.1f", Math.tan(vTheta) * 1000)
        ).getString();
    }

    @Override
    protected List<Pair<String, String>> getRegistryEntries() {
        return RailModelRegistry.elements.entrySet().stream()
                .filter(e -> !e.getValue().name.getString().isEmpty())
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

    public static void applyBrushToPickedRail(CompoundTag railBrushProp) {
        if (railBrushProp == null) return;
        if (RailPicker.pickedRail == null) return;
        boolean updated = false;
        if (railBrushProp.contains("ModelKey")) {
            ((RailExtraSupplier) RailPicker.pickedRail).setModelKey(railBrushProp.getString("ModelKey"));
            updated = true;
        }
        if (railBrushProp.contains("VerticalCurveRadius")) {
            ((RailExtraSupplier) RailPicker.pickedRail).setVerticalCurveRadius(railBrushProp.getFloat("VerticalCurveRadius"));
            updated = true;
        }
        if (updated) {
            PacketUpdateRail.sendUpdateC2S(RailPicker.pickedRail, RailPicker.pickedPosStart, RailPicker.pickedPosEnd);
        }
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
