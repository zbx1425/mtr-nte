package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.model.Model;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.*;

public class RailRenderDispatcher {

    private final HashMap<Rail, RailSpan> railSpanMap = new HashMap<>();
    private final LinkedList<RailSpan> railSpanList = new LinkedList<>();
    private int lastRebuildCycleIndex = -1;

    private final HashSet<Rail> currentFrameRails = new HashSet<>();

    public static boolean isHoldingRailItem = false;

    protected Model commonRailModel;
    protected Model sidingRailModel;

    public void setModel(Model commonRailModel, Model sidingRailModel) {
        this.commonRailModel = commonRailModel;
        this.sidingRailModel = sidingRailModel;
    }

    private void addRail(Rail rail) {
        if (railSpanMap.containsKey(rail)) return;
        RailSpan railSpan = new RailSpan(rail, rail.railType == RailType.SIDING ? sidingRailModel : commonRailModel);
        railSpanMap.put(rail, railSpan);
        railSpanList.add(railSpan);
    }

    private void removeRail(Rail rail) {
        if (!railSpanMap.containsKey(rail)) return;
        RailSpan railSpan = railSpanMap.get(rail);
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
        for (RailSpan chunk : railSpanList) {
            chunk.close();
        }
        railSpanList.clear();
    }

    public void updateAndEnqueueAll(Level level, BatchManager batchManager, Matrix4f viewMatrix) {
        isHoldingRailItem = Minecraft.getInstance().player != null && RenderTrains.isHoldingRailRelated(Minecraft.getInstance().player);

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

        for (RailSpan chunk : railSpanList) {
            if (!chunk.bufferBuilt) chunk.rebuildBuffer(level);
            chunk.renderAll(batchManager, viewMatrix);
        }
    }
}
