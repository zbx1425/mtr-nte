package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

#if MC_VERSION >= "11904"
    @Inject(method = "renderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V", ordinal = 0, shift = At.Shift.AFTER))
    void renderGuiItem(PoseStack poseStack, ItemStack itemStack, int i, int j, BakedModel bakedModel, CallbackInfo ci) {
        if (bakedModel.usesBlockLight()) {
            Lighting.setupFor3DItems();
        } else {
            Lighting.setupForFlatItems();
        }
    }
#else
    @Redirect(method = "renderGuiItem(Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V", ordinal = 0))
    void renderGuiItem() {
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
#endif
}
