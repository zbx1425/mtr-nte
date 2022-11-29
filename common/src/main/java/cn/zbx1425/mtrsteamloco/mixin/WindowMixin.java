package cn.zbx1425.mtrsteamloco.mixin;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyArgs(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"), remap = false)
    private void ctor(Args args) {
        switch (args.<Integer>get(0)) {
            case GLFW.GLFW_CONTEXT_VERSION_MAJOR:
            case GLFW.GLFW_CONTEXT_VERSION_MINOR:
                args.set(1, Math.max(args.<Integer>get(1), 3));
                break;
        }
    }
}