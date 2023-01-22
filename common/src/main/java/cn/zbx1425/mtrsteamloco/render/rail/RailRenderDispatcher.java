package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.mtrsteamloco.mixin.LevelRendererAccessor;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.mappings.Text;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.Level;

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
                    return new InstancedRailChunk(chunkId, RailModelRegistry.getUploadedModel(bakedRail.modelKey));
                } else {
                    return new MeshBuildingRailChunk(chunkId, RailModelRegistry.getRawModel(bakedRail.modelKey));
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
            if (chunk != null && !chunk.isDirty) {
                // Minecraft.getInstance().player.displayClientMessage(Text.literal("Light update: " + x + ", " + z), false);
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
        /* if (railsToAdd.size() > 0) {
            Minecraft.getInstance().player.displayClientMessage(Text.literal("Rails to add: " + railsToAdd.size()), false);
        } */
        for (RailWrapper rail : railsToAdd) addRail(rail);
        HashSet<RailWrapper> railsToRemove = new HashSet<>(railRefMap.keySet());
        railsToRemove.removeAll(currentFrameRails);
        /* if (railsToRemove.size() > 0) {
            Minecraft.getInstance().player.displayClientMessage(Text.literal("Rails to remove: " + railsToRemove.size()), false);
        } */
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
                    if (buffersRebuilt < 1) chunk.rebuildBuffer(level); // One per frame
                    // chunk.rebuildBuffer(level);
                    buffersRebuilt++;
                }
                if (cullingFrustum.isVisible(chunk.boundingBox)) {
                    chunk.enqueue(batchManager, shaderProp);
                }
            }
        }
        /* if (buffersRebuilt > 0) {
            Minecraft.getInstance().player.displayClientMessage(Text.literal("Rebuilt: " + buffersRebuilt), false);
        } */

    }
}
