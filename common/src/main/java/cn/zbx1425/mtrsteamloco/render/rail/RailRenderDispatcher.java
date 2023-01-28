package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.mtrsteamloco.mixin.LevelRendererAccessor;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcer.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class RailRenderDispatcher {

    private final HashMap<RailWrapper, BakedRail> railRefMap = new HashMap<>();
    private final HashMap<String, HashMap<Long, RailChunkBase>> railChunkMap = new HashMap<>();
    private boolean isInstanced;

    private final HashSet<RailWrapper> currentFrameRails = new HashSet<>();

    public static boolean isHoldingRailItem = false;

    private void addRail(RailWrapper rail) {
        if (railRefMap.containsKey(rail)) return;
        BakedRail bakedRail = new BakedRail(rail.rail);
        railRefMap.put(rail, bakedRail);
        HashMap<Long, RailChunkBase> chunkMap = railChunkMap.get(bakedRail.modelKey);
        if (chunkMap == null) return;
        for (long chunkId : bakedRail.coveredChunks.keySet()) {
            chunkMap.computeIfAbsent(chunkId, ignored -> {
                if (isInstanced) {
                    return new InstancedRailChunk(chunkId, bakedRail.modelKey);
                } else {
                    return new MeshBuildingRailChunk(chunkId, bakedRail.modelKey);
                }
            }).addRail(bakedRail);
        }
    }

    private void removeRail(RailWrapper rail) {
        if (!railRefMap.containsKey(rail)) return;
        BakedRail bakedRail = railRefMap.get(rail);
        railRefMap.remove(rail);
        HashMap<Long, RailChunkBase> chunkMap = railChunkMap.get(bakedRail.modelKey);
        if (chunkMap == null) return;
        for (long chunkId : bakedRail.coveredChunks.keySet()) {
            chunkMap.get(chunkId).removeRail(bakedRail);
        }
    }

    public void registerRail(Rail rail) {
        if (rail.railType == RailType.NONE) return;
        currentFrameRails.add(new RailWrapper(rail));
    }

    public void clearRail() {
        currentFrameRails.clear();
        railRefMap.clear();
        for (HashMap<Long, RailChunkBase> chunkMap : railChunkMap.values()) {
            for (RailChunkBase chunk : chunkMap.values()) {
                chunk.close();
            }
            chunkMap.clear();
        }
        railChunkMap.clear();
        for (String key : RailModelRegistry.rawElements.keySet()) {
            railChunkMap.put(key, new HashMap<>());
        }
    }

    public void registerLightUpdate(int x, int z) {
        long chunkId = BakedRail.chunkIdFromSectPos(x, z);
        for (HashMap<Long, RailChunkBase> chunkMap : railChunkMap.values()) {
            RailChunkBase chunk = chunkMap.get(chunkId);
            if (chunk != null) {
                chunk.isDirty = true;
            }
        }
    }

    public void updateAndEnqueueAll(Level level, BatchManager batchManager, Matrix4f viewMatrix) {
        isHoldingRailItem = Minecraft.getInstance().player != null && RenderTrains.isHoldingRailRelated(Minecraft.getInstance().player);

        boolean shouldBeInstanced = ClientConfig.getRailRenderLevel() == 3;
        if (isInstanced != shouldBeInstanced) clearRail();
        isInstanced = shouldBeInstanced;

        HashSet<RailWrapper> railsToAdd = new HashSet<>(currentFrameRails);
        railsToAdd.removeAll(railRefMap.keySet());
        for (RailWrapper rail : railsToAdd) addRail(rail);
        HashSet<RailWrapper> railsToRemove = new HashSet<>(railRefMap.keySet());
        railsToRemove.removeAll(currentFrameRails);
        for (RailWrapper rail : railsToRemove) removeRail(rail);
        currentFrameRails.clear();

        int buffersRebuilt = 0;
        Frustum cullingFrustum = ((LevelRendererAccessor)Minecraft.getInstance().levelRenderer).getCullingFrustum();
        ShaderProp shaderProp = new ShaderProp().setViewMatrix(viewMatrix);
        for (HashMap<Long, RailChunkBase> chunkMap : railChunkMap.values()) {
            for (Iterator<Map.Entry<Long, RailChunkBase>> it = chunkMap.entrySet().iterator(); it.hasNext(); ) {
                RailChunkBase chunk = it.next().getValue();
                if (chunk.containingRails.size() == 0) {
                    chunk.close();
                    it.remove();
                    continue;
                }
                if (chunk.isDirty) {
#if DEBUG
                    chunk.rebuildBuffer(level);
#else
                    if (buffersRebuilt < 1) chunk.rebuildBuffer(level); // One per frame
#endif
                    buffersRebuilt++;
                }
                if (cullingFrustum.isVisible(chunk.boundingBox)) {
                    chunk.enqueue(batchManager, shaderProp);
                }
            }
        }
    }

    public void drawBoundingBoxes(PoseStack matrixStack, VertexConsumer buffer) {
        for (HashMap<Long, RailChunkBase> chunkMap : railChunkMap.values()) {
            for (RailChunkBase chunk : chunkMap.values()) {
                boolean isChunkEven = chunk.isEven();
                LevelRenderer.renderLineBox(matrixStack, buffer, chunk.boundingBox,
                        1.0f, isChunkEven ? 1.0f : 0.0f, isChunkEven ? 0.0f : 1.0f, 1.0f);
                for (ArrayList<Matrix4f> rail : chunk.containingRails.values()) {
                    for (Matrix4f pieceMat : rail) {
                        final Vector3f lightPos = pieceMat.getTranslationPart();
                        final BlockPos lightBlockPos = new BlockPos(lightPos.x(), lightPos.y() + 0.1, lightPos.z());
                        LevelRenderer.renderLineBox(matrixStack, buffer, new AABB(lightBlockPos),
                                1.0f, isChunkEven ? 1.0f : 0.0f, isChunkEven ? 0.0f : 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
