package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.mtrsteamloco.gui.SelectButtonsScreen;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    /*
    @ModifyVariable(method = "getProjectionMatrix", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    double getProjectionMatrixHead(double value) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof SelectButtonsScreen && ((SelectButtonsScreen)currentScreen).isSelecting()) {
            return 100;
        } else {
            return value;
        }
    }
    */

    @Inject(method = "getProjectionMatrix", at = @At("TAIL"), cancellable = true)
    void getProjectionMatrixTail(double fov, CallbackInfoReturnable<com.mojang.math.Matrix4f> cir) {
        Screen currentScreen = Minecraft.getInstance().screen;
        if (currentScreen instanceof SelectButtonsScreen && ((SelectButtonsScreen)currentScreen).isSelecting()) {
            Matrix4f result = Matrix4f.createTranslateMatrix(0.5f, 0f, 0f);
            result.multiply(Matrix4f.createScaleMatrix(0.8f, 0.8f, 1f));
            result.multiply(cir.getReturnValue());
            cir.setReturnValue(result);
        }
    }
}
