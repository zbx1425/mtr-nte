package cn.zbx1425.mtrsteamloco.render.rail;

import io.netty.buffer.Unpooled;
import mtr.data.Rail;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;

public class RailWrapper {

    // Rail in MTR doesn't have hashCode().
    // And it keeps recreating new instances for the same stuff from network sync packets.
    // This is to make my HashMap and HashSet actually work.

    private static final FriendlyByteBuf hashBuilder = new FriendlyByteBuf(Unpooled.buffer());

    public final Rail rail;

    private final byte[] dataBytes;
    private final int hashCode;

    public RailWrapper(Rail rail) {
        hashBuilder.clear();
        rail.writePacket(hashBuilder);
        byte[] dataBytes = new byte[hashBuilder.writerIndex()];
        hashBuilder.getBytes(0, dataBytes);
        this.rail = rail;
        this.dataBytes = dataBytes;
        this.hashCode = Arrays.hashCode(dataBytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(dataBytes, ((RailWrapper)o).dataBytes);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
