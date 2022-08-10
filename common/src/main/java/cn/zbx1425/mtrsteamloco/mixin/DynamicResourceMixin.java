package cn.zbx1425.mtrsteamloco.mixin;

import mtr.client.ClientCache;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientCache.DynamicResource.class)
public class DynamicResourceMixin {

    @Redirect(method = "remove", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;"))
    public AbstractTexture redirectGetTexture(TextureManager instance, ResourceLocation resourceLocation) {
        AbstractTexture result = instance.getTexture(resourceLocation, MissingTextureAtlasSprite.getTexture());
        if (result != MissingTextureAtlasSprite.getTexture()) {
            return result;
        } else {
            return null;
        }
    }
}
