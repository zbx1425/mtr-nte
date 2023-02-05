package cn.zbx1425.mtrsteamloco.mixin;

import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.util.AttrUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlStateManager.class)
public abstract class GlStateManagerMixin {

    @Redirect(method = "glShaderSource", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"))
    private static String glShaderSource(StringBuilder instance) {
        String inputContent = instance.toString();
        String[] contentParts = inputContent.split("void main");
        boolean shouldPatch = contentParts[1].contains("gl_Position")
                && contentParts[0].contains("Position") && contentParts[0].contains("Normal")
                && !contentParts[0].contains("ChunkOffset");
        if (!shouldPatch) return inputContent;

        // Multiply ModelViewMat into Normal if it isn't taken into account (in vanilla mix_light)
        StringBuilder functionSb = new StringBuilder();
        for (String line : contentParts[1].split("\n")) {
            if (line.contains("iris_Normal")) {
                if (!line.contains("iris_ModelViewMat") && !line.contains("iris_NormalMat")) {
                    // Not sure how Iris handles it? Might break
                    functionSb.append(line.replaceAll("\\biris_Normal\\b", "normalize(mat3(iris_ModelViewMat) * iris_Normal)"));
                } else {
                    functionSb.append(line);
                }
            } else if (line.contains("Normal")) {
                if (!line.contains("ModelViewMat")) {
                    functionSb.append(line.replaceAll("\\bNormal\\b", "normalize(mat3(ModelViewMat) * Normal)"));
                } else {
                    functionSb.append(line);
                }
            } else {
                functionSb.append(line);
            }
            functionSb.append('\n');
        }
        return contentParts[0] + "void main" + functionSb.toString();
    }

#if MC_VERSION >= "11903"
    @Redirect(method = "setupGui3DDiffuseLighting", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotationYXZ(FFF)Lorg/joml/Matrix4f;", remap = false))
    private static org.joml.Matrix4f setupGuiFlatDiffuseLighting_Matrix4f_rotationYXZ(org.joml.Matrix4f instance, float angleY, float angleX, float angleZ) {
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getModelViewMatrix()).copy();
        AttrUtil.zeroTranslation(viewRotation);
        return instance.set(viewRotation.asMoj()).rotateYXZ(angleY, angleX, angleZ);
    }

    @Redirect(method = "setupGuiFlatDiffuseLighting", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scaling(FFF)Lorg/joml/Matrix4f;", remap = false))
    private static org.joml.Matrix4f setupGuiFlatDiffuseLighting_Matrix4f_scaling(org.joml.Matrix4f instance, float x, float y, float z) {
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getModelViewMatrix()).copy();
        AttrUtil.zeroTranslation(viewRotation);
        return instance.set(viewRotation.asMoj()).scale(x, y, z);
    }
#else
    @Redirect(method = "setupGui3DDiffuseLighting", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix4f;setIdentity()V"))
    private static void setupGui3DDiffuseLighting_Matrix4f_setIdentity(com.mojang.math.Matrix4f instance) {
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getModelViewMatrix()).copy();
        AttrUtil.zeroTranslation(viewRotation);
        instance.load(viewRotation.asMoj());
    }

    @Redirect(method = "setupGuiFlatDiffuseLighting", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix4f;setIdentity()V"))
    private static void setupGuiFlatDiffuseLighting_Matrix4f_setIdentity(com.mojang.math.Matrix4f instance) {
        Matrix4f viewRotation = new Matrix4f(RenderSystem.getModelViewMatrix()).copy();
        AttrUtil.zeroTranslation(viewRotation);
        instance.load(viewRotation.asMoj());
    }
#endif

}
