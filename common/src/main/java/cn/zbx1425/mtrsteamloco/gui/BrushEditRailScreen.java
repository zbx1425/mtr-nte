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
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "12000"
import net.minecraft.client.gui.GuiGraphics;
#endif
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class BrushEditRailScreen extends SelectListScreen {

    private boolean isSelectingModel = false;

    private static final String INSTRUCTION_LINK = "https://www.zbx1425.cn/nautilus/mtr-nte/#/railmodel";
    private final WidgetLabel lblInstruction = new WidgetLabel(0, 0, 0, Text.translatable("gui.mtrsteamloco.eye_candy.tip_resource_pack"), () -> {
        this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(INSTRUCTION_LINK);
            }
            this.minecraft.setScreen(this);
        }, INSTRUCTION_LINK, true));
    });

    private static Rail pickedRail = null;
    private static BlockPos pickedPosStart = BlockPos.ZERO;
    private static BlockPos pickedPosEnd = BlockPos.ZERO;

    public BrushEditRailScreen() {
        super(Text.literal("Select rail arguments"));
        if (pickedRail == null) acquirePickInfoWhenUse();
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
            scrollList.visible = true;
            loadSelectPage(key -> !key.equals(modelKey));
            lblInstruction.alignR = true;
            IDrawing.setPositionAndWidth(lblInstruction, width / 2 + SQUARE_SIZE, height - SQUARE_SIZE - TEXT_HEIGHT, 0);
            lblInstruction.setWidth(width / 2 - SQUARE_SIZE * 2);
            addRenderableWidget(lblInstruction);
        } else {
            scrollList.visible = false;
            loadMainPage();
        }
    }

    @Override
    protected void onBtnClick(String btnKey) {
        updateBrushTag(compoundTag -> compoundTag.putString("ModelKey", btnKey));
    }

    Button btnSetDefaultRadius = UtilitiesClient.newButton(
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_max"),
            sender -> updateRadius(0, true)
    );
    Button btnSetNoRadius = UtilitiesClient.newButton(
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_set_none"),
            sender -> updateRadius(-1, true)
    );
    WidgetLabel valuesLabel = new WidgetLabel(SQUARE_SIZE, SQUARE_SIZE * 7 + 6, width - SQUARE_SIZE * 2,
            Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_irl_ref"));
    WidgetBetterTextField radiusInput = new WidgetBetterTextField("", 8);

    private void loadMainPage() {
        CompoundTag brushTag = getBrushTag();
        addRenderableWidget(new WidgetLabel(SQUARE_SIZE, SQUARE_SIZE + 2, width - SQUARE_SIZE * 2,
                Text.translatable("gui.mtrsteamloco.brush_edit_rail.brush_hint")));

        boolean enableModelKey = brushTag != null && brushTag.contains("ModelKey");
        String modelKey = ((RailExtraSupplier)pickedRail).getModelKey();
        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 2, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_model_key"),
                checked -> {
                    updateBrushTag(compoundTag -> {
                        if (checked) {
                            compoundTag.putString("ModelKey", modelKey);
                        } else {
                            compoundTag.remove("ModelKey");
                        }
                    });
                    loadPage();
                }
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
        float vertCurveRadius = ((RailExtraSupplier)pickedRail).getVerticalCurveRadius();
        addRenderableWidget(new WidgetBetterCheckbox(SQUARE_SIZE, SQUARE_SIZE * 5, COLUMN_WIDTH * 2, SQUARE_SIZE,
                Text.translatable("gui.mtrsteamloco.brush_edit_rail.enable_vertical_curve_radius"),
                checked -> {
                    updateBrushTag(compoundTag -> {
                        if (checked) {
                            compoundTag.putFloat("VerticalCurveRadius", vertCurveRadius);
                        } else {
                            compoundTag.remove("VerticalCurveRadius");
                        }
                    });
                    loadPage();
                }
        )).setChecked(enableVertCurveRadius);
        if (enableVertCurveRadius) {
            updateRadius(vertCurveRadius, false);
            radiusInput.setResponder(text -> {
                if (!text.isEmpty()) {
                    try {
                        float newRadius = Float.parseFloat(text);
                        Rail rail = pickedRail;
                        if (rail != null) {
                            int H = Math.abs(((RailExtraSupplier) rail).getHeight());
                            double L = rail.getLength();
                            double maxRadius = (H == 0) ? 0 : (H * H + L * L) / (H * 4);
                            if (newRadius < maxRadius) {
                                radiusInput.setTextColor(0xE0E0E0);
                            } else {
                                radiusInput.setTextColor(0xEEEE00);
                            }
                        } else {
                            radiusInput.setTextColor(0xEEEE00);
                        }
                        updateRadius(newRadius, true);
                    } catch (Exception ignored) {
                        radiusInput.setTextColor(0xFF0000);
                    }
                }
            });
            IDrawing.setPositionAndWidth(addRenderableWidget(radiusInput), SQUARE_SIZE, SQUARE_SIZE * 6, COLUMN_WIDTH * 2);
            IDrawing.setPositionAndWidth(addRenderableWidget(btnSetDefaultRadius),
                    SQUARE_SIZE + COLUMN_WIDTH * 2, SQUARE_SIZE * 6, COLUMN_WIDTH);
            IDrawing.setPositionAndWidth(addRenderableWidget(btnSetNoRadius),
                    SQUARE_SIZE + COLUMN_WIDTH * 3, SQUARE_SIZE * 6, COLUMN_WIDTH);
            valuesLabel.setWidth(width - SQUARE_SIZE * 2);
            addRenderableWidget(valuesLabel);
        }
    }

    private void updateRadius(float newRadius, boolean send) {
        valuesLabel.setMessage(Text.literal(getVerticalValueText(newRadius)));
        btnSetDefaultRadius.active = newRadius != 0;
        btnSetNoRadius.active = newRadius >= 0;

        String expectedText;
        if (newRadius <= 0) {
            expectedText = "";
        } else {
            expectedText = Integer.toString((int) newRadius);
        }
        if (!expectedText.equals(radiusInput.getValue())) {
            radiusInput.setValue(expectedText);
            radiusInput.moveCursorToStart();
        }

        if (send) {
            updateBrushTag(compoundTag -> {
                compoundTag.putFloat("VerticalCurveRadius", newRadius);
            });
        }
    }

    private String getVerticalValueText(float verticalRadius) {
        Rail rail = pickedRail;
        if (rail == null) return "(???)";
        int H = Math.abs(((RailExtraSupplier)rail).getHeight());
        double L = rail.getLength();
        double maxRadius = (H == 0) ? 0 : (H * H + L * L) / (H * 4);
        double gradient;
        if (verticalRadius < 0) {
            gradient = H / L * 1000;
        } else if (verticalRadius == 0 || verticalRadius > maxRadius) {
            gradient = Math.tan(RailExtraSupplier.getVTheta(rail, maxRadius)) * 1000;
        } else {
            gradient = Math.tan(RailExtraSupplier.getVTheta(rail, verticalRadius)) * 1000;
        }
        return Text.translatable("gui.mtrsteamloco.brush_edit_rail.vertical_curve_radius_values",
                String.format("%.1f", maxRadius), String.format("%.1f", gradient)
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
        applyBrushToPickedRail(nteTag, false);
        PacketUpdateHoldingItem.sendUpdateC2S();
    }

    public static void acquirePickInfoWhenUse() {
        pickedRail = RailPicker.pickedRail;
        pickedPosStart = RailPicker.pickedPosStart;
        pickedPosEnd = RailPicker.pickedPosEnd;
    }

    public static void applyBrushToPickedRail(CompoundTag railBrushProp, boolean isBatchApply) {
        if (railBrushProp == null) return;
        if (pickedRail == null) return;
        RailExtraSupplier pickedExtra = (RailExtraSupplier) pickedRail;
        boolean propertyUpdated = false;
        if (railBrushProp.contains("ModelKey") &&
                !railBrushProp.getString("ModelKey").equals(pickedExtra.getModelKey())) {
            pickedExtra.setModelKey(railBrushProp.getString("ModelKey"));
            propertyUpdated = true;
        }
        if (railBrushProp.contains("VerticalCurveRadius") &&
                railBrushProp.getFloat("VerticalCurveRadius") != pickedExtra.getVerticalCurveRadius()) {
            pickedExtra.setVerticalCurveRadius(railBrushProp.getFloat("VerticalCurveRadius"));
            propertyUpdated = true;
        }
        if (isBatchApply && !propertyUpdated) {
            // Right-click again to reverse the direction
            pickedExtra.setRenderReversed(!pickedExtra.getRenderReversed());
        }
        PacketUpdateRail.sendUpdateC2S(pickedRail, pickedPosStart, pickedPosEnd);
    }

    @Override
#if MC_VERSION >= "12000"
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
#else
    public void render(PoseStack guiGraphics, int mouseX, int mouseY, float partialTick) {
#endif
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isSelectingModel) {
            renderSelectPage(guiGraphics);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
