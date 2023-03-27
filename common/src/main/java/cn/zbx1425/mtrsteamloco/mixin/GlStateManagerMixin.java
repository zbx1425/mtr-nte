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

}
