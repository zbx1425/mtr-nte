package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.render.ByteBufferOutputStream;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.model.VertArrays;
import cn.zbx1425.sowcer.object.VertBuf;
import cn.zbx1425.sowcer.util.OffHeapAllocator;
import cn.zbx1425.sowcer.vertex.VertAttrMapping;
import cn.zbx1425.sowcer.vertex.VertAttrSrc;
import cn.zbx1425.sowcer.vertex.VertAttrState;
import cn.zbx1425.sowcer.vertex.VertAttrType;
import cn.zbx1425.sowcerext.model.RawModel;
import com.google.common.io.LittleEndianDataOutputStream;
import mtr.data.Rail;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MeshBuildingBakedRail extends BakedRailBase {

    private RawModel railModel;
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

    protected MeshBuildingBakedRail(Rail rail, RawModel railModel) {
        super(rail);
        this.railModel = railModel;
    }

    @Override
    public void rebuildBuffer(Level world) {
        if (rail.getLength() > MAX_RAIL_LENGTH_ACCEPTABLE) return;
        super.rebuildBuffer(world);

        RawModel combinedModel = new RawModel();
        rail.render((x1, z1, x2, z2, x3, z3, x4, z4, y1, y2) -> {
            double xc = (x1 + x4) / 2;
            double yc = (y1 + y2) / 2;
            double zc = (z1 + z4) / 2;
            final BlockPos pos2 = new BlockPos(x1, y1 + 0.1, z1);
            final int light2 = LightTexture.pack(world.getBrightness(LightLayer.BLOCK, pos2), world.getBrightness(LightLayer.SKY, pos2));
            Matrix4f lookAtMat = lookAt((float) xc, (float) yc, (float) zc, (float) x4, (float) y2, (float) z4, 0.25f);
            combinedModel.appendTransformed(railModel, lookAtMat, light2);
        }, 0, 0);
        if (vertArrays != null) vertArrays.close();
        if (uploadedCombinedModel != null) uploadedCombinedModel.close();
        uploadedCombinedModel = combinedModel.upload(RAIL_MAPPING);
        vertArrays = VertArrays.createAll(uploadedCombinedModel, RAIL_MAPPING, null);
    }

    @Override
    public void enqueue(BatchManager batchManager, ShaderProp shaderProp) {
        if (vertArrays == null) return;
        int color = RailRenderDispatcher.isHoldingRailItem ? (rail.railType.color << 8 | 0xFF) : -1;
        batchManager.enqueue(vertArrays, new EnqueueProp(new VertAttrState()
                .setColor(color)
                .setModelMatrix(shaderProp.viewMatrix)
        ), ShaderProp.DEFAULT);
    }

    @Override
    public void close() {
        if (vertArrays != null) vertArrays.close();
        if (uploadedCombinedModel != null) uploadedCombinedModel.close();
    }
}
