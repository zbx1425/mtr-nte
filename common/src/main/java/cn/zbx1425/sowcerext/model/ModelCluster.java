package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import org.lwjgl.opengl.KHRDebug;

import java.io.Closeable;

public class ModelCluster implements Closeable {

    private final RawModel source;
    private final VertArrays opaqueParts;
    private final RawModel translucentParts;

    public ModelCluster(RawModel source, VertAttrMapping mapping) {
        this.source = source;
        RawModel rawOpaqueParts = new RawModel();
        this.translucentParts = new RawModel();
        for (RawMesh mesh : source.meshList.values()) {
            if (mesh.materialProp.translucent) {
                translucentParts.append(mesh);
            } else {
                rawOpaqueParts.append(mesh);
            }
        }
        this.opaqueParts = VertArrays.createAll(rawOpaqueParts.upload(mapping), mapping, null);
    }

    public void renderOptimized(BatchManager batchManager, MultiBufferSource vertexConsumers, Matrix4f pose, int light) {
        KHRDebug.glDebugMessageInsert(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, KHRDebug.GL_DEBUG_TYPE_MARKER,
                0, KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, "RenderOptimized " + (source.sourceLocation == null ? "unknown" : source.sourceLocation.toString()));
        int shaderLightmapUV = AttrUtil.exchangeLightmapUVBits(light);
        batchManager.enqueue(opaqueParts, new EnqueueProp(
                new VertAttrState().setColor(255, 255, 255, 255).setLightmapUV(shaderLightmapUV).setModelMatrix(pose)
        ), ShaderProp.DEFAULT);
        if (translucentParts.meshList.size() > 0) {
            translucentParts.writeBlazeBuffer(vertexConsumers, pose, light);
        }
    }

    public void renderUnoptimized(MultiBufferSource vertexConsumers, Matrix4f pose, int light) {
        source.writeBlazeBuffer(vertexConsumers, pose, light);
    }

    @Override
    public void close(){
        opaqueParts.close();
    }
}
