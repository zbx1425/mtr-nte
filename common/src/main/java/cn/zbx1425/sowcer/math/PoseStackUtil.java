package cn.zbx1425.sowcer.math;

import com.mojang.blaze3d.vertex.PoseStack;

public class PoseStackUtil {

    public static void rotX(PoseStack matrices, float rad) {
        matrices.mulPose(com.mojang.math.Vector3f.XP.rotation(rad));
    }

    public static void rotY(PoseStack matrices, float rad) {
        matrices.mulPose(com.mojang.math.Vector3f.YP.rotation(rad));
    }

    public static void rotZ(PoseStack matrices, float rad) {
        matrices.mulPose(com.mojang.math.Vector3f.ZP.rotation(rad));
    }

    public static void mulTranslate(PoseStack matrices, float x, float y, float z) {
        Matrix4f mat = new Matrix4f();
        mat.translate(x, y, z);
        matrices.mulPoseMatrix(mat.asMoj());
    }
}
