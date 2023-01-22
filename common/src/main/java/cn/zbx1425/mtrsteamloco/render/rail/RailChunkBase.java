package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class RailChunkBase implements Closeable {

    public Long chunkId;
    public HashMap<BakedRail, ArrayList<Matrix4f>> containingRails = new HashMap<>();

    public boolean isDirty = false;

    public RailChunkBase(long chunkId) {
        this.chunkId = chunkId;
    }

    public void addRail(BakedRail rail) {
        containingRails.put(rail, rail.coveredChunks.get(chunkId));
        isDirty = true;
    }

    public void removeRail(BakedRail rail) {
        containingRails.remove(rail);
        isDirty = true;
    }

    public void rebuildBuffer(Level world) {
        isDirty = false;
    }
    public abstract void enqueue(BatchManager batchManager, ShaderProp shaderProp);

    @Override
    public void close() {

    }
}
