package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.google.common.io.LittleEndianDataOutputStream;
import net.minecraft.world.level.Level;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class RenderRailChunk implements Closeable {

    public final ChunkPos pos;

    public final HashSet<RailSpan> containingRails = new HashSet<>();

    private final InstanceBuf instanceBuf = new InstanceBuf(0);
    private final VertArrays vertArrays;

    public static VertAttrMapping RAIL_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.INSTANCE_BUF)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.INSTANCE_BUF)
            .build();

    public RenderRailChunk(ChunkPos pos, Model railModel) {
        this.pos = pos;
        vertArrays = VertArrays.createAll(railModel, RAIL_MAPPING, instanceBuf);
    }

    public void rebuildBuffer(Level world) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        LittleEndianDataOutputStream dataOutputStream = new LittleEndianDataOutputStream(byteArrayOutputStream);
        for (RailSpan rail : containingRails) {
            rail.writeToBuffer(world, pos, dataOutputStream);
        }
        ByteBuffer byteBuf = OffHeapAllocator.allocate(byteArrayOutputStream.size());
        byteBuf.put(byteArrayOutputStream.toByteArray());
        instanceBuf.size = byteArrayOutputStream.size() / RAIL_MAPPING.strideInstance;
        instanceBuf.upload(byteBuf, VertBuf.USAGE_DYNAMIC_DRAW);
        OffHeapAllocator.free(byteBuf);
    }

    public void renderAll(BatchManager batchManager, EnqueueProp enqueueProp, ShaderProp shaderProp) {
        if (instanceBuf.size < 1) return;
        batchManager.enqueue(vertArrays, enqueueProp, shaderProp);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        instanceBuf.close();
    }
}
