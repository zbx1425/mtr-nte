package cn.zbx1425.mtrsteamloco.render.rail;

import java.util.Objects;

public class ChunkPos {

    public int regX, regZ;
    public static final int regionSize = 32;

    public ChunkPos(double x, double z) {
        update(x, z);
    }

    public void update(double x, double z) {
        regX = (int)Math.floor(x / regionSize);
        regZ = (int)Math.floor(z / regionSize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPos chunkPos = (ChunkPos) o;
        return regX == chunkPos.regX && regZ == chunkPos.regZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(regX, regZ);
    }
}
