package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Window.class)
public class WindowMixin {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"), remap = false)
    private void ctor(int hint, int value) {
        switch (hint) {
            case GLFW.GLFW_CONTEXT_VERSION_MAJOR:
            case GLFW.GLFW_CONTEXT_VERSION_MINOR:
                GLFW.glfwWindowHint(hint, Math.max(value, 3));
                break;
            default:
                GLFW.glfwWindowHint(hint, value);
                break;
        }
    }
}