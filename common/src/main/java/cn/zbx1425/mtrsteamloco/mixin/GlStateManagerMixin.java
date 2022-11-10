package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.sowcer.shader.PatchingResourceProvider;
import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlStateManager.class)
public class GlStateManagerMixin {

    @Redirect(method = "glShaderSource", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"))
    private static String redirectShaderSource(StringBuilder instance) {
        String result = instance.toString();
        // if (result.startsWith("#zbxflag vsh patch")) {
            result = result.replace("#zbxflag vsh patch\n", "");
            result = PatchingResourceProvider.patchVertexShaderSource(result);
        // }
        return result;
    }
}
