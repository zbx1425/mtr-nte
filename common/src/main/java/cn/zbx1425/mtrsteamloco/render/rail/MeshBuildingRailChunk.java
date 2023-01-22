package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.ArrayList;

public class MeshBuildingRailChunk extends RailChunkBase {

    private final RawModel railModel;

    private Model uploadedCombinedModel;
    private VertArrays vertArrays;

    private static final VertAttrMapping RAIL_MAPPING = new VertAttrMapping.Builder()
            .set(VertAttrType.POSITION, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.COLOR, VertAttrSrc.GLOBAL)
            .set(VertAttrType.UV_TEXTURE, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.UV_LIGHTMAP, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.NORMAL, VertAttrSrc.VERTEX_BUF)
            .set(VertAttrType.MATRIX_MODEL, VertAttrSrc.GLOBAL)
            .build();

    protected MeshBuildingRailChunk(Long chunkId, String modelKey) {
        super(chunkId, modelKey);
        this.railModel = RailModelRegistry.getRawModel(modelKey);
    }

    @Override
    public void rebuildBuffer(Level world) {
        super.rebuildBuffer(world);

        float yMin = 256, yMax = -64;
        RawModel combinedModel = new RawModel();
        for (ArrayList<Matrix4f> railSpan : containingRails.values()) {
            for (Matrix4f pieceMat : railSpan) {
                final Vector3f lightPos = pieceMat.getTranslationPart();
                yMin = Math.min(yMin, lightPos.y());
                yMax = Math.max(yMax, lightPos.y());
                final BlockPos lightBlockPos = new BlockPos(lightPos.x(), lightPos.y() + 0.1, lightPos.z());
                final int light = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, lightBlockPos), world.getBrightness(LightLayer.SKY, lightBlockPos));
                combinedModel.appendTransformed(railModel, pieceMat, light);
            }
        }
        if (vertArrays != null) vertArrays.close();
        if (uploadedCombinedModel != null) uploadedCombinedModel.close();
        uploadedCombinedModel = combinedModel.upload(RAIL_MAPPING);
        vertArrays = VertArrays.createAll(uploadedCombinedModel, RAIL_MAPPING, null);

        if (yMin > yMax) yMin = yMax;
        setBoundingBox(yMin, yMax);
    }

    @Override
    public void enqueue(BatchManager batchManager, ShaderProp shaderProp) {
        if (vertArrays == null) return;
        // int color = RailRenderDispatcher.isHoldingRailItem ? (rail.railType.color << 8 | 0xFF) : -1;
        batchManager.enqueue(vertArrays, new EnqueueProp(new VertAttrState()
                .setColor(-1)
                .setModelMatrix(shaderProp.viewMatrix)
        ), ShaderProp.DEFAULT);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        if (uploadedCombinedModel != null) uploadedCombinedModel.close();
    }
}
