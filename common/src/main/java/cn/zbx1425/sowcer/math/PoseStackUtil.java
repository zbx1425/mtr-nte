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
}
