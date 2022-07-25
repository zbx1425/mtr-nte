package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.EnqueueProp;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.model.Model;
import com.mojang.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailType;
import mtr.render.RenderTrains;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.*;

public class RailRenderDispatcher {

    private final HashMap<ChunkPos, RenderRailChunk> renderChunks = new HashMap<>();
    private final HashMap<Rail, RailSpan> railSpans = new HashMap<>();

    private final HashSet<Rail> currentFrameRails = new HashSet<>();

    private final HashSet<RenderRailChunk> chunksToRebuild = new HashSet<>();

    public static boolean isHoldingRailItem = false;

    protected Model railModel;

    public void setModel(Model railModel) {
        this.railModel = railModel;
    }

    private void addRail(Rail rail) {
        if (railSpans.containsKey(rail)) return;
        RailSpan railSpan = new RailSpan(rail);
        for (ChunkPos reg : railSpan.coveredChunks) {
            renderChunks.putIfAbsent(reg, new RenderRailChunk(reg, railModel));
            RenderRailChunk chunk = renderChunks.get(reg);
            chunk.containingRails.add(railSpan);
            chunksToRebuild.add(chunk);
        }
        railSpans.put(rail, railSpan);
    }

    private void removeRail(Rail rail) {
        if (!railSpans.containsKey(rail)) return;
        RailSpan railSpan = railSpans.get(rail);
        for (ChunkPos reg : railSpan.coveredChunks) {
            RenderRailChunk chunk = renderChunks.get(reg);
            chunk.containingRails.remove(railSpan);
            chunksToRebuild.add(chunk);
        }
        railSpans.remove(rail);
    }

    public void registerRail(Rail rail) {
        if (rail.railType == RailType.NONE) return;
        currentFrameRails.add(rail);
    }

    private static final Random random = new Random();

    public void updateAndEnqueueAll(Level level, BatchManager batchManager, Matrix4f viewMatrix) {
        isHoldingRailItem = Minecraft.getInstance().player != null && RenderTrains.isHoldingRailRelated(Minecraft.getInstance().player);

        HashSet<Rail> railsToAdd = new HashSet<>(currentFrameRails);
        railsToAdd.removeAll(railSpans.keySet());
        for (Rail rail : railsToAdd) addRail(rail);
        HashSet<Rail> railsToRemove = new HashSet<>(railSpans.keySet());
        railsToRemove.removeAll(currentFrameRails);
        for (Rail rail : railsToRemove) removeRail(rail);
        currentFrameRails.clear();
        for (Iterator<Map.Entry<ChunkPos, RenderRailChunk>> it = renderChunks.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<ChunkPos, RenderRailChunk> entry = it.next();
            if (entry.getValue().containingRails.size() == 0) {
                entry.getValue().close();
                chunksToRebuild.remove(entry.getValue());
                it.remove();
            }
        }

        if (chunksToRebuild.size() == 0 && renderChunks.size() != 0) {
            // TODO Sth better
            Optional<RenderRailChunk> randomChunk = renderChunks.values().stream().skip(random.nextInt(renderChunks.size())).findFirst();
            randomChunk.ifPresent(chunksToRebuild::add);
        }

        if (chunksToRebuild.size() > 0) {
            Optional<RenderRailChunk> chunkToRebuild = chunksToRebuild.stream().findFirst();
            chunkToRebuild.get().rebuildBuffer(level);
            chunksToRebuild.remove(chunkToRebuild.get());
        }

        for (RenderRailChunk chunk : renderChunks.values()) {
            chunk.renderAll(batchManager, EnqueueProp.DEFAULT, new ShaderProp().setViewMatrix(viewMatrix));
        }
    }
}
