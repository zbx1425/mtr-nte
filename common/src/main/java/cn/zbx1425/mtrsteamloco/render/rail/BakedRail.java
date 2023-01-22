package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailType;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;

public class BakedRail {

    public HashMap<Long, ArrayList<Matrix4f>> coveredChunks = new HashMap<>();

    public String modelKey;

    public BakedRail(Rail rail) {
        if (rail.railType == RailType.SIDING) {
            modelKey = "rail_siding";
        } else {
            modelKey = "rail";
        }

        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            float xc = (float)((x1 + x4) / 2);
            float yc = (float)((y1 + y2) / 2);
            float zc = (float)((z1 + z4) / 2);
            coveredChunks
                    .computeIfAbsent(chunkIdFromWorldPos(xc, zc), ignored -> new ArrayList<>())
                    .add(getLookAtMat(xc, yc, zc, (float) x4,(float)y2, (float)z4, 0.25f));
        }, 0, 0);
    }

    public static long chunkIdFromWorldPos(float bpX, float bpZ) {
        return ((long)((int)bpX >> (4 + 1)) << 32) | ((long)((int)bpZ >> (4 + 1)) & 0xFFFFFFFFL);
    }

    public static long chunkIdFromSectPos(int spX, int spZ) {
        return ((long)(spX >> 1) << 32) | ((long)(spZ >> 1) & 0xFFFFFFFFL);
    }

    public static Matrix4f getLookAtMat(float posX, float posY, float posZ, float tgX, float tgY, float tgZ, float len) {
        Matrix4f matrix4f = Matrix4f.translation(posX, posY, posZ);

        final float yaw = (float) Mth.atan2(tgX - posX, tgZ - posZ);
        final float pitch = (float) Math.asin((tgY - posY) * (1f / len));

        matrix4f.rotateY((float) Math.PI + yaw);
        matrix4f.rotateX(pitch);

        return matrix4f;
    }
}
