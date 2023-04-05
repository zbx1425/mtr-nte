package cn.zbx1425.mtrsteamloco.render;

import cn.zbx1425.mtrsteamloco.mixin.RailAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mtr.Blocks;
import mtr.client.ClientData;
import mtr.client.IDrawing;
import mtr.data.Rail;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

public class RailPicker {

    public static Rail pickedRail;
    public static BlockPos pickedPosStart;
    public static BlockPos pickedPosEnd;

    public static void pick() {
        pickedRail = null;
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.getCameraEntity();
        if (entity == null || minecraft.level == null) return;
        HitResult hitResult = entity.pick(20.0, 0.0f, false);
        if (hitResult.getType() != HitResult.Type.BLOCK) return;
        BlockPos pos = ((BlockHitResult)hitResult).getBlockPos();
        pickedPosStart = pos;
        BlockState blockState = minecraft.level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof mtr.block.BlockNode)) return;
        if (ClientData.RAILS.get(pos) == null) return;

        Optional<Map.Entry<BlockPos, Rail>> closestEntry = ClientData.RAILS.get(pos).entrySet().stream().min(Comparator.comparingDouble(entry ->
                Mth.degreesDifferenceAbs((float) -Math.toDegrees(Math.atan2(entry.getKey().getX() - pos.getX(), entry.getKey().getZ() - pos.getZ())), entity.getYRot())
        ));
        if (closestEntry.isEmpty()) return;
        pickedPosEnd = closestEntry.get().getKey();
        pickedRail = closestEntry.get().getValue();
    }

    public static void render(PoseStack matrices, MultiBufferSource vertexConsumers) {
        if (pickedRail == null) return;

        RailAccessor rail = (RailAccessor)pickedRail;
        double length = pickedRail.getLength();
        double gradient = Math.abs(rail.invokeGetPositionY(length / 2 - 0.5) - rail.invokeGetPositionY(length / 2 + 0.5)) * 1000;
        double radius;
        if (!rail.getIsStraight1() && !rail.getIsStraight2()) {
            radius = Math.min(rail.getR1(), rail.getR2());
        } else if (!rail.getIsStraight1()) {
            radius = rail.getR1();
        } else if (!rail.getIsStraight2()) {
            radius = rail.getR2();
        } else {
            radius = 0;
        }

        String[] contents = new String[] {
            String.format("L%.1fm", pickedRail.getLength()),
            String.format("R%.1fm P%.1f‰", radius, gradient)
        };

        matrices.pushPose();
        matrices.translate(pickedPosStart.getX(), pickedPosStart.getY(), pickedPosStart.getZ());
        matrices.translate(0.5, 0.5, 0.5);
        matrices.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        matrices.scale(-0.025F, -0.025F, 0.025F);
        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int)(opacity * 255.0F) << 24;
        Font font = Minecraft.getInstance().font;
        int yOffset = -contents.length / 2 * font.lineHeight;
        for (var text : contents) {
            if (text != null && !StringUtils.isEmpty(text)) {
                float xOffset = (float) (-font.width(text) / 2);
#if MC_VERSION >= "11904"
                font.drawInBatch(text, xOffset, yOffset, 0xFFFFFFFF, false, matrices.last().pose(), vertexConsumers, Font.DisplayMode.SEE_THROUGH, bgColor, LightTexture.FULL_BRIGHT, false);
#else
                font.drawInBatch(text, xOffset, yOffset, 0xFFFFFFFF, false, matrices.last().pose(), vertexConsumers, false, bgColor, LightTexture.FULL_BRIGHT);
#endif
            }
            yOffset += font.lineHeight + 2;
        }
        matrices.popPose();

        IDrawing.drawLine(matrices, vertexConsumers,
                pickedPosStart.getX() + 0.5f, pickedPosStart.getY() + 0.2f, pickedPosStart.getZ() + 0.5f,
                pickedPosEnd.getX() + 0.5f, pickedPosEnd.getY() + 0.2f, pickedPosEnd.getZ() + 0.5f, 255, 255, 180);
    }
}
