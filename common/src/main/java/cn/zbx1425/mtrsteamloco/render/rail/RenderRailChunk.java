package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.InstanceBuf;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import com.google.common.io.LittleEndianDataOutputStream;
import com.mojang.blaze3d.platform.MemoryTracker;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL33;

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

    public static ShaderProp RAIL_SHADER_PROP = new ShaderProp().setEyeTransformInModelMatrix(false);

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
        ByteBuffer byteBuf = MemoryTracker.create(byteArrayOutputStream.size());
        byteBuf.put(byteArrayOutputStream.toByteArray());
        instanceBuf.size = byteArrayOutputStream.size() / 72;
        instanceBuf.upload(byteBuf);
    }

    public void renderAll(BatchManager batchManager, EnqueueProp enqueueProp) {
        batchManager.enqueue(vertArrays, enqueueProp, RAIL_SHADER_PROP);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        instanceBuf.close();
    }
}
