package cn.zbx1425.mtrsteamloco.render.rail;

import cn.zbx1425.mtrsteamloco.data.RailModelRegistry;
import cn.zbx1425.sowcer.batch.BatchManager;
import cn.zbx1425.sowcer.batch.ShaderProp;
import cn.zbx1425.sowcer.math.Matrix4f;
import mtr.data.Rail;
import mtr.data.RailAngle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public abstract class RailChunkBase implements Closeable {

    public Long chunkId;
    public AABB boundingBox;
    public HashMap<BakedRail, ArrayList<Matrix4f>> containingRails = new HashMap<>();

    public final String modelKey;

    protected float modelYMin;
    protected float modelYMax;

    public boolean isDirty = false;
    public boolean bufferBuilt = false;
    public double cameraDistManhattanXZ = 0;

    public RailChunkBase(long chunkId, String modelKey) {
        this.chunkId = chunkId;
        this.modelKey = modelKey;
        long boundary = RailModelRegistry.getProperty(modelKey).boundingBox;
        modelYMin = Float.intBitsToFloat((int)(boundary >> 32));
        modelYMax = Float.intBitsToFloat((int)(boundary & 0xFFFFFFFFL));
        setBoundingBox(0, 0);
    }

    protected void setBoundingBox(float yMin, float yMax) {
        int posXMin = (int)(chunkId >> 32) << (4 + BakedRail.POS_SHIFT);
        int posZMin = (int)(chunkId & 0xFFFFFFFFL) << (4 + BakedRail.POS_SHIFT);
        int span = 1 << (4 + BakedRail.POS_SHIFT);
        boundingBox = new AABB(posXMin, yMin + modelYMin - 1, posZMin,
                posXMin + span, yMax + modelYMax + 1, posZMin + span);
    }

    public boolean isEven() { // Just for ease of debugging to show a checkerboard pattern.
        return ((int)(chunkId >> 32) + (int)(chunkId & 0xFFFFFFFFL)) % 2 == 0;
    }

    public ChunkPos getChunkPos() {
        return new ChunkPos((int)(chunkId >> 32) << BakedRail.POS_SHIFT, (int)(chunkId & 0xFFFFFFFFL) << BakedRail.POS_SHIFT);
    }

    public boolean containsYSection(int yMin, int yMax) {
        return (yMin << 4) < boundingBox.minY || (yMax << 4) > boundingBox.maxY;
    }

    public double getCameraDistManhattanXZ(Vec3 cameraPos) {
        cameraDistManhattanXZ = Math.abs(cameraPos.x - (boundingBox.minX + boundingBox.maxX) / 2)
                + Math.abs(cameraPos.z - (boundingBox.minZ + boundingBox.maxZ) / 2);
        return cameraDistManhattanXZ;
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
        bufferBuilt = true;
    }
    public abstract void enqueue(BatchManager batchManager, ShaderProp shaderProp);

    @Override
    public void close() {

    }
}
