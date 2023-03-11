package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.util.AttrUtil;
import mtr.data.Rail;
import mtr.data.RailType;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.HashMap;

public class BakedRail {

    public HashMap<Long, ArrayList<Matrix4f>> coveredChunks = new HashMap<>();

    public static final int POS_SHIFT = 1;

    public String modelKey;
    public int color;

    public BakedRail(Rail rail) {
        modelKey = RailRenderDispatcher.getPreferredRailModel(rail);
        color = AttrUtil.argbToBgr(rail.railType.color | 0xFF000000);

        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            float xc = (float)((x1 + x4) / 2);
            float yc = (float)((y1 + y2) / 2);
            float zc = (float)((z1 + z4) / 2);
            coveredChunks
                    .computeIfAbsent(chunkIdFromWorldPos(Mth.floor(xc), Mth.floor(zc)), ignored -> new ArrayList<>())
                    .add(getLookAtMat(xc, yc, zc, (float) x4,(float)y2, (float)z4, 0.25f));
        }, 0, 0);
    }

    public static long chunkIdFromWorldPos(float bpX, float bpZ) {
        return ((long)((int)bpX >> (4 + POS_SHIFT)) << 32) | ((long)((int)bpZ >> (4 + POS_SHIFT)) & 0xFFFFFFFFL);
    }

    public static long chunkIdFromSectPos(int spX, int spZ) {
        return ((long)(spX >> POS_SHIFT) << 32) | ((long)(spZ >> POS_SHIFT) & 0xFFFFFFFFL);
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
