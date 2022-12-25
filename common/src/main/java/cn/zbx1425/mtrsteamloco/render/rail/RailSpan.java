package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.ByteBufferOutputStream;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.google.common.io.LittleEndianDataOutputStream;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import mtr.data.Rail;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RailSpan implements Closeable {

    public Rail rail;

    private final InstanceBuf instanceBuf = new InstanceBuf(0);
    private final VertArrays vertArrays;

    private int instanceCount = 0;

    public boolean bufferBuilt = false;

    public static VertAttrMapping RAIL_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.INSTANCE_BUF)
            .build();

    public RailSpan(Rail rail, Model railModel) {
        this.rail = rail;
        vertArrays = VertArrays.createAll(railModel, RAIL_MAPPING, instanceBuf);
        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            instanceCount++;
        }, 0, 0);
    }


    public void rebuildBuffer(Level world) {
        bufferBuilt = true;

        ByteBuffer byteBuf = OffHeapAllocator.allocate(instanceCount * RAIL_MAPPING.strideInstance);
        ByteBufferOutputStream byteArrayOutputStream = new ByteBufferOutputStream(byteBuf, false);
        LittleEndianDataOutputStream oStream = new LittleEndianDataOutputStream(byteArrayOutputStream);

        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            final BlockPos pos2 = new BlockPos(x1, y1 + 0.1, z1);
            try {
                double xc = (x1 + x4) / 2;
                double yc = (y1 + y2) / 2;
                double zc = (z1 + z4) / 2;
                final int light2 = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, pos2), world.getBrightness(LightLayer.SKY, pos2));
                oStream.writeInt(light2);
                byte[] mat = lookAt(new Vector3f((float) xc, (float) yc, (float) zc), new Vector3f((float) x4, (float) y2, (float) z4), new Vector3f(0, 1, 0));
                oStream.write(mat);
            } catch (IOException ex) {
                Main.LOGGER.error("Failed building 3DRail instance VBO:", ex);
            }
        }, 0, 0);

        instanceBuf.size = instanceCount;
        instanceBuf.upload(byteBuf, VertBuf.USAGE_DYNAMIC_DRAW);
        OffHeapAllocator.free(byteBuf);
    }

    public void renderAll(BatchManager batchManager, Matrix4f viewMatrix) {
        if (instanceBuf.size < 1) return;
        int color = RailRenderDispatcher.isHoldingRailItem ? (rail.railType.color << 8 | 0xFF) : -1;
        batchManager.enqueue(
                vertArrays,
                new EnqueueProp(new VertAttrState().setColor(color)),
                new ShaderProp().setViewMatrix(viewMatrix)
        );
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        instanceBuf.close();
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
        Matrix4f matrix4f = Matrix4f.translation(position.x(), position.y(), position.z());

        final float yaw = (float) Mth.atan2(target.x() - position.x(), target.z() - position.z());
        final float pitch = (float) Math.asin((target.y() - position.y()) * 4); // TODO hardcoded

        matrix4f.rotateY((float) Math.PI + yaw);
        matrix4f.rotateX(pitch);

        matrix4f.store(fb);

        return result;
    }
}
