package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.mtrsteamloco.render.ByteBufferOutputStream;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
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
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;

public class InstancedRailChunk extends RailChunkBase {

    private final InstanceBuf instanceBuf = new InstanceBuf(0);
    private final VertArrays vertArrays;

    private static final VertAttrMapping RAIL_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_OVERLAY, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.INSTANCE_BUF)
            .build();

    public InstancedRailChunk(Long chunkId, String modelKey) {
        super(chunkId, modelKey);
        vertArrays = VertArrays.createAll(RailModelRegistry.getUploadedModel(modelKey), RAIL_MAPPING, instanceBuf);
    }

    @Override
    public void rebuildBuffer(Level world) {
        super.rebuildBuffer(world);

        int instanceCount = containingRails.values().stream().mapToInt(ArrayList::size).sum();
        float yMin = 256, yMax = -64;

        ByteBuffer byteBuf = OffHeapAllocator.allocate(instanceCount * RAIL_MAPPING.strideInstance);
        ByteBufferOutputStream byteArrayOutputStream = new ByteBufferOutputStream(byteBuf, false);
        LittleEndianDataOutputStream oStream = new LittleEndianDataOutputStream(byteArrayOutputStream);

        for (Map.Entry<BakedRail, ArrayList<Matrix4f>> entry : containingRails.entrySet()) {
            ArrayList<Matrix4f> railSpan = entry.getValue();
            for (Matrix4f pieceMat : railSpan) {
                try {
                    oStream.writeInt(entry.getKey().color);

                    final Vector3f lightPos = pieceMat.getTranslationPart();
                    yMin = Math.min(yMin, lightPos.y());
                    yMax = Math.max(yMax, lightPos.y());
                    final BlockPos lightBlockPos = new BlockPos(lightPos.x(), lightPos.y() + 0.1, lightPos.z());
                    final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, lightBlockPos), world.getBrightness(LightLayer.SKY, lightBlockPos));
                    oStream.writeInt(light);

                    byte[] lookAtBytes = new byte[4 * 16];
                    ByteBuffer matByteBuf = ByteBuffer.wrap(lookAtBytes).order(ByteOrder.nativeOrder());
                    FloatBuffer matFloatBuf = matByteBuf.asFloatBuffer();
                    pieceMat.store(matFloatBuf);
                    oStream.write(lookAtBytes);

                    if (RAIL_MAPPING.paddingInstance > 0) oStream.writeByte(0);
                } catch (IOException ignored) {

                }
            }
        }

        instanceBuf.size = instanceCount;
        instanceBuf.upload(byteBuf, VertBuf.USAGE_DYNAMIC_DRAW);
        OffHeapAllocator.free(byteBuf);

        if (yMin > yMax) yMin = yMax;
        setBoundingBox(yMin, yMax);
    }

    @Override
    public void enqueue(BatchManager batchManager, ShaderProp shaderProp) {
        if (instanceBuf.size < 1) return;
        VertAttrState attrState = new VertAttrState().setOverlayUVNoOverlay();
        if (!RailRenderDispatcher.isHoldingRailItem) attrState.setColor(-1);
        batchManager.enqueue(vertArrays, new EnqueueProp(attrState, VertAttrType.COLOR), shaderProp);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        instanceBuf.close();
    }
}
