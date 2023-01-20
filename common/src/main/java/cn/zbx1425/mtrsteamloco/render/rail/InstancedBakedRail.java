package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.ByteBufferOutputStream;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.google.common.io.LittleEndianDataOutputStream;
import mtr.data.Rail;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class InstancedBakedRail extends BakedRailBase {

    private final InstanceBuf instanceBuf = new InstanceBuf(0);
    private final VertArrays vertArrays;

    private static final VertAttrMapping RAIL_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.INSTANCE_BUF)
            .build();

    public InstancedBakedRail(Rail rail, Model railModel) {
        super(rail);
        vertArrays = VertArrays.createAll(railModel, RAIL_MAPPING, instanceBuf);
    }

    @Override
    public void rebuildBuffer(Level world) {
        if (rail.getLength() > MAX_RAIL_LENGTH_ACCEPTABLE) return;
        super.rebuildBuffer(world);

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

                Matrix4f lookAtMat = lookAt((float) xc, (float) yc, (float) zc, (float) x4, (float) y2, (float) z4, 0.25f);
                byte[] lookAtBytes = new byte[4 * 16];
                ByteBuffer matByteBuf = ByteBuffer.wrap(lookAtBytes).order(ByteOrder.nativeOrder());
                FloatBuffer matFloatBuf = matByteBuf.asFloatBuffer();
                lookAtMat.store(matFloatBuf);
                oStream.write(lookAtBytes);
            } catch (IOException ex) {
                Main.LOGGER.error("Failed building 3DRail instance VBO:", ex);
            }
        }, 0, 0);

        instanceBuf.size = instanceCount;
        instanceBuf.upload(byteBuf, VertBuf.USAGE_DYNAMIC_DRAW);
        OffHeapAllocator.free(byteBuf);
    }

    @Override
    public void enqueue(BatchManager batchManager, ShaderProp shaderProp) {
        if (instanceBuf.size < 1) return;
        int color = RailRenderDispatcher.isHoldingRailItem ? (rail.railType.color << 8 | 0xFF) : -1;
        batchManager.enqueue(vertArrays, new EnqueueProp(new VertAttrState().setColor(color)), shaderProp);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        instanceBuf.close();
    }
}
