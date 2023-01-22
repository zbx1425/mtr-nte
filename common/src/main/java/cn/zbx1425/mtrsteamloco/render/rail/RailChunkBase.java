package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class RailChunkBase implements Closeable {

    public Long chunkId;
    public AABB boundingBox;
    public HashMap<BakedRail, ArrayList<Matrix4f>> containingRails = new HashMap<>();

    public boolean isDirty = false;

    public RailChunkBase(long chunkId) {
        this.chunkId = chunkId;
        int posXMin = (int)(chunkId >> 32) << (4 + 1);
        int posZMin = (int)(chunkId | 0xFFFFFFFFL) << (4 + 1);
        int span = 1 << (4 + 1);
        boundingBox = new AABB(posXMin, -64, posZMin, posXMin + span, 256, posZMin + span);
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
