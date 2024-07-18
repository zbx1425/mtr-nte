package cn.zbx1425.mtrsteamloco.render.scripting.util;

import cn.zbx1425.sowcer.util.GlStateTracker;
import cn.zbx1425.sowcerext.model.ModelCluster;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.reuse.ModelManager;
import com.mojang.blaze3d.systems.RenderSystem;

public class DynamicModelHolder {

    private ModelCluster uploadedModel;

    public void uploadLater(RawModel rawModel) {
        RawModel finalRawModel = rawModel.copyForMaterialChanges();
        finalRawModel.sourceLocation = null;
        RenderSystem.recordRenderCall(() -> {
            boolean needProtection = !GlStateTracker.isStateProtected;
            if (needProtection) GlStateTracker.capture();
            ModelCluster lastUploadedModel = uploadedModel;
            uploadedModel = new ModelCluster(finalRawModel, ModelManager.DEFAULT_MAPPING);
            if (lastUploadedModel != null) lastUploadedModel.close();
            if (needProtection) GlStateTracker.restore();
        });
    }

    public ModelCluster getUploadedModel() {
        return uploadedModel;
    }

    public void close() {
        RenderSystem.recordRenderCall(() -> {
            if (uploadedModel != null) {
                uploadedModel.close();
                uploadedModel = null;
            }
        });
    }
}
