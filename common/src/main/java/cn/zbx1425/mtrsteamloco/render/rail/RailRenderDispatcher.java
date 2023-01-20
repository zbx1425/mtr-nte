package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.RawModel;
import com.google.common.collect.HashMultimap;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;

import java.util.*;

public class RailRenderDispatcher {

    private final HashMap<Rail, BakedRailBase> railRefMap = new HashMap<>();
    private final HashMultimap<Long, BakedRailBase> railChunkMap = HashMultimap.create();
    private boolean isInstanced;

    private final HashSet<Rail> currentFrameRails = new HashSet<>();
    private final HashSet<BakedRailBase> buffersToRebuild = new HashSet<>();

    public static boolean isHoldingRailItem = false;

    protected RawModel rawCommonRailModel;
    protected Model commonRailModel;
    protected RawModel rawSidingRailModel;
    protected Model sidingRailModel;

    public void setModel(RawModel rawCommonRailModel, Model commonRailModel, RawModel rawSidingRailModel, Model sidingRailModel) {
        this.rawCommonRailModel = rawCommonRailModel;
        this.commonRailModel = commonRailModel;
        this.rawSidingRailModel = rawSidingRailModel;
        this.sidingRailModel = sidingRailModel;
    }

    private void addRail(Rail rail) {
        if (railRefMap.containsKey(rail)) return;
        BakedRailBase railSpan;
        if (isInstanced) {
            railSpan = new InstancedBakedRail(rail, rail.railType == RailType.SIDING ? sidingRailModel : commonRailModel);
        } else {
            railSpan = new MeshBuildingBakedRail(rail, rail.railType == RailType.SIDING ? rawSidingRailModel : rawCommonRailModel);
        }
        railRefMap.put(rail, railSpan);
        for (long chunkPos : railSpan.coveredChunks) railChunkMap.put(chunkPos, railSpan);
    }

    private void removeRail(Rail rail) {
        if (!railRefMap.containsKey(rail)) return;
        BakedRailBase railSpan = railRefMap.get(rail);
        railSpan.close();
        railRefMap.remove(rail);
        buffersToRebuild.remove(railSpan);
        for (long chunkPos : railSpan.coveredChunks) railChunkMap.remove(chunkPos, railSpan);
    }

    public void registerRail(Rail rail) {
        if (rail.railType == RailType.NONE) return;
        currentFrameRails.add(rail);
    }

    public void clearRail() {
        currentFrameRails.clear();
        for (BakedRailBase chunk : railRefMap.values()) {
            chunk.close();
        }
        railRefMap.clear();
        railChunkMap.clear();
        synchronized (buffersToRebuild) {
            buffersToRebuild.clear();
        }
    }

    public void registerLightUpdate(int x, int z) {
        long chunkPos = (long)x << 32 | (long)z;
        synchronized (buffersToRebuild) {
            buffersToRebuild.addAll(railChunkMap.get(chunkPos));
        }
    }

    public void updateAndEnqueueAll(Level level, BatchManager batchManager, Matrix4f viewMatrix) {
        isHoldingRailItem = Minecraft.getInstance().player != null && RenderTrains.isHoldingRailRelated(Minecraft.getInstance().player);

        boolean shouldBeInstanced = ClientConfig.getRailRenderLevel() == 3;
        if (isInstanced != shouldBeInstanced) clearRail();
        isInstanced = shouldBeInstanced;

        HashSet<Rail> railsToAdd = new HashSet<>(currentFrameRails);
        railsToAdd.removeAll(railRefMap.keySet());
        for (Rail rail : railsToAdd) addRail(rail);
        HashSet<Rail> railsToRemove = new HashSet<>(railRefMap.keySet());
        railsToRemove.removeAll(currentFrameRails);
        currentFrameRails.clear();

        synchronized (buffersToRebuild) {
            for (Rail rail : railsToRemove) removeRail(rail);
            for (BakedRailBase railSpan : buffersToRebuild) {
                railSpan.rebuildBuffer(level);
            }
            buffersToRebuild.clear();
        }

        ShaderProp shaderProp = new ShaderProp().setViewMatrix(viewMatrix);
        for (BakedRailBase railSpan : railRefMap.values()) {
            if (!railSpan.bufferBuilt) railSpan.rebuildBuffer(level);
            railSpan.enqueue(batchManager, shaderProp);
        }
    }
}
