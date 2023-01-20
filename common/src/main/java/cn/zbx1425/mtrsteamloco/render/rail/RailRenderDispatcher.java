package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.math.Matrix4f;
import cn.zbx1425.sowcerext.model.RawModel;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.*;

public class RailRenderDispatcher {

    private final HashMap<Rail, BakedRailBase> railSpanMap = new HashMap<>();
    private final LinkedList<BakedRailBase> railSpanList = new LinkedList<>();
    private boolean isInstanced;
    private int lastRebuildCycleIndex = -1;

    private final HashSet<Rail> currentFrameRails = new HashSet<>();

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
        if (railSpanMap.containsKey(rail)) return;
        BakedRailBase railSpan;
        if (isInstanced) {
            railSpan = new InstancedBakedRail(rail, rail.railType == RailType.SIDING ? sidingRailModel : commonRailModel);
        } else {
            railSpan = new MeshBuildingBakedRail(rail, rail.railType == RailType.SIDING ? rawSidingRailModel : rawCommonRailModel);
        }
        railSpanMap.put(rail, railSpan);
        railSpanList.add(railSpan);
    }

    private void removeRail(Rail rail) {
        if (!railSpanMap.containsKey(rail)) return;
        BakedRailBase railSpan = railSpanMap.get(rail);
        railSpan.close();
        railSpanMap.remove(rail);
        railSpanList.remove(railSpan);
    }

    public void registerRail(Rail rail) {
        if (rail.railType == RailType.NONE) return;
        currentFrameRails.add(rail);
    }

    public void clearRail() {
        railSpanMap.clear();
        currentFrameRails.clear();
        lastRebuildCycleIndex = -1;
        for (BakedRailBase chunk : railSpanList) {
            chunk.close();
        }
        railSpanList.clear();
    }

    public void updateAndEnqueueAll(Level level, BatchManager batchManager, Matrix4f viewMatrix) {
        isHoldingRailItem = Minecraft.getInstance().player != null && RenderTrains.isHoldingRailRelated(Minecraft.getInstance().player);

        boolean shouldBeInstanced = ClientConfig.getRailRenderLevel() == 3;
        if (isInstanced != shouldBeInstanced) {
            clearRail();
        }
        isInstanced = shouldBeInstanced;

        HashSet<Rail> railsToAdd = new HashSet<>(currentFrameRails);
        railsToAdd.removeAll(railSpanMap.keySet());
        for (Rail rail : railsToAdd) addRail(rail);
        HashSet<Rail> railsToRemove = new HashSet<>(railSpanMap.keySet());
        railsToRemove.removeAll(currentFrameRails);
        for (Rail rail : railsToRemove) removeRail(rail);
        currentFrameRails.clear();

        if (railSpanList.size() > 0) {
            // Cycle through each chunk and rebuild the mesh every frame.
            // Might be better to somehow listen for lighting updates?
            // As for performance impact, I suppose if it's to be a lag spike anyway,
            // it won't hurt to spread it out so that it's more noticeable.
            lastRebuildCycleIndex++;
            if (lastRebuildCycleIndex >= railSpanList.size()) lastRebuildCycleIndex = 0;
            railSpanList.get(lastRebuildCycleIndex).rebuildBuffer(level);
        }

        ShaderProp shaderProp = new ShaderProp().setViewMatrix(viewMatrix);
        for (BakedRailBase chunk : railSpanList) {
            if (!chunk.bufferBuilt) chunk.rebuildBuffer(level);
            chunk.enqueue(batchManager, shaderProp);
        }
    }
}
