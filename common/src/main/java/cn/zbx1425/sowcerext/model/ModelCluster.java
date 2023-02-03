package cn.zbx1425.sowcerext.model;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.util.AttrUtil;
import cn.zbx1425.sowcer.util.Profiler;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;

import java.io.Closeable;

public class ModelCluster implements Closeable {

    public final VertArrays uploadedOpaqueParts;
    public final RawModel opaqueParts;
    public final RawModel translucentParts;

    public ModelCluster(RawModel source, VertAttrMapping mapping) {
        this.translucentParts = new RawModel();
        this.opaqueParts = new RawModel();
        for (RawMesh mesh : source.meshList.values()) {
            if (mesh.materialProp.translucent) {
                translucentParts.append(mesh);
            } else {
                opaqueParts.append(mesh);
            }
        }
        translucentParts.distinct();
        if (mapping == null) {
            // If mapping is null: skip uploading, this cluster will not have optimized rendering
            this.uploadedOpaqueParts = null;
        } else {
            this.uploadedOpaqueParts = VertArrays.createAll(opaqueParts.upload(mapping), mapping, null);
        }
    }

    public void renderOpaqueOptimized(BatchManager batchManager, Matrix4f pose, int light, Profiler profiler) {
        // KHRDebug.glDebugMessageInsert(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, KHRDebug.GL_DEBUG_TYPE_MARKER,
        //        0, KHRDebug.GL_DEBUG_SEVERITY_NOTIFICATION, "RenderOptimized " + (source.sourceLocation == null ? "unknown" : source.sourceLocation.toString()));
        int shaderLightmapUV = AttrUtil.exchangeLightmapUVBits(light);
        batchManager.enqueue(uploadedOpaqueParts, new EnqueueProp(
                new VertAttrState()
                        .setColor(255, 255, 255, 255).setOverlayUVNoOverlay()
                        .setLightmapUV(shaderLightmapUV).setModelMatrix(pose)
        ), ShaderProp.DEFAULT);
    }

    public void renderOpaqueUnoptimized(MultiBufferSource vertexConsumers, Matrix4f pose, int light, Profiler profiler) {
        opaqueParts.writeBlazeBuffer(vertexConsumers, pose, light, profiler);
    }

    public void renderTranslucent(MultiBufferSource vertexConsumers, Matrix4f pose, int light, Profiler profiler) {
        translucentParts.writeBlazeBuffer(vertexConsumers, pose, light, profiler);
    }

    public boolean isUploaded() {
        return uploadedOpaqueParts != null;
    }

    @Override
    public void close(){
        uploadedOpaqueParts.close();
    }
}
