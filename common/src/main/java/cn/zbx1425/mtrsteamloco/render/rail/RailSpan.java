package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.sowcer.util.AttrUtil;
import com.google.common.io.LittleEndianDataOutputStream;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.data.Rail;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashSet;

public class RailSpan {

    public final HashSet<ChunkPos> coveredChunks = new HashSet<>();

    public Rail rail;

    public RailSpan(Rail rail) {
        this.rail = rail;
        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            coveredChunks.add(new ChunkPos(x1, z1));
        }, 0, 0);
    }

    public void writeToBuffer(Level world, ChunkPos pos, LittleEndianDataOutputStream oStream) {
        ChunkPos cmpPos = new ChunkPos(0, 0);
        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            cmpPos.update(x1, z1);
            if (!cmpPos.equals(pos)) return;
            final BlockPos pos2 = new BlockPos(x1, y1 + 0.1, z1);
            try {
                double xc = (x1 + x4) / 2;
                double yc = (y1 + y2) / 2;
                double zc = (z1 + z4) / 2;
                oStream.writeInt(RailRenderDispatcher.isHoldingRailItem ? AttrUtil.argbToBgr(rail.railType.color) : -1);
                final int light2 = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, pos2), world.getBrightness(LightLayer.SKY, pos2));
                oStream.writeInt(light2);
                byte[] mat = lookAt(new Vector3f((float) xc, (float) yc, (float) zc), new Vector3f((float) x4, (float) y2, (float) z4), new Vector3f(0, 1, 0));
                oStream.write(mat);
            } catch (IOException ex) {
                Main.LOGGER.error("Failed building 3DRail instance VBO:", ex);
            }
        }, 0, 0);
    }

    private byte[] lookAt(Vector3f position, Vector3f target, Vector3f up) {
        Vector3f f = target.copy();
        f.sub(position);
        f.normalize();
        Vector3f s = f.copy();
        s.cross(up);
        s.normalize();
        Vector3f u = s.copy();
        u.cross(f);
        u.normalize();

        byte[] result = new byte[4 * 16];
        ByteBuffer byteBuf = ByteBuffer.wrap(result).order(ByteOrder.nativeOrder());
        FloatBuffer fb = byteBuf.asFloatBuffer();
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.translate(position.x(), position.y(), position.z());

        final float yaw = (float) Mth.atan2(target.x() - position.x(), target.z() - position.z());
        final float pitch = (float) Math.asin((target.y() - position.y()) * 4); // TODO hardcoded

        matrix4f.rotateY((float) Math.PI + yaw);
        matrix4f.rotateX(pitch);

        matrix4f.store(fb);
        /*fb.put(0, s.x());
        fb.put(4, s.y());
        fb.put(8, s.z());
        fb.put(1, u.x());
        fb.put(5, u.y());
        fb.put(9, u.z());
        fb.put(2, -f.x());
        fb.put(6, -f.y());
        fb.put(10, -f.z());
        fb.put(3, 0F);
        fb.put(7, 0F);
        fb.put(11, 0F);
        fb.put(12, -s.dot(position));
        fb.put(13, -u.dot(position));
        fb.put(14, f.dot(position));
        fb.put(15, 1F);*/

        return result;
    }
}
