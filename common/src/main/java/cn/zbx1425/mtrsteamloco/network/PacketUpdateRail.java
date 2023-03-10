package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.data.RailExtraSupplier;
import cn.zbx1425.mtrsteamloco.mixin.RailwayDataAccessor;
import io.netty.buffer.Unpooled;
import mtr.Registry;
import mtr.RegistryClient;
import mtr.data.Rail;
import mtr.data.RailwayData;
import mtr.packet.IPacket;
import mtr.packet.PacketTrainDataGuiServer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Map;

public class PacketUpdateRail {

    public static ResourceLocation PACKET_UPDATE_RAIL = new ResourceLocation(Main.MOD_ID, "update_rail");

    public void sendUpdateC2S(Rail newState, BlockPos posStart, BlockPos posEnd) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeResourceLocation(level.dimension().location());
        packet.writeBlockPos(posStart);
        packet.writeBlockPos(posEnd);
        newState.writePacket(packet);

        RegistryClient.sendToServer(PACKET_UPDATE_RAIL, packet);
    }

    public static void receiveUpdateC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
#if MC_VERSION >= "11903"
        ResourceKey<Level> levelKey = packet.readResourceKey(net.minecraft.core.registries.Registries.DIMENSION);
#else
        ResourceKey<Level> levelKey = ResourceKey.create(net.minecraft.core.Registry.DIMENSION_REGISTRY, packet.readResourceLocation());
#endif
        BlockPos posStart = packet.readBlockPos();
        BlockPos posEnd = packet.readBlockPos();
        Rail newState = new Rail(packet);
        server.execute(() -> {
            ServerLevel level = server.getLevel(levelKey);
            if (level == null) return;

            RailwayData railwayData = RailwayData.getInstance(level);
            Map<BlockPos, Map<BlockPos, Rail>> rails = ((RailwayDataAccessor)railwayData).getRails();
            Rail railForward = rails.get(posStart).get(posEnd);
            Rail railBackward = rails.get(posEnd).get(posStart);
            if (railForward == null || railBackward == null) return;

            ((RailExtraSupplier)railForward).setModelKey(((RailExtraSupplier)newState).getModelKey());
            ((RailExtraSupplier)railBackward).setModelKey(((RailExtraSupplier)newState).getModelKey());

            final FriendlyByteBuf outboundPacket = new FriendlyByteBuf(Unpooled.buffer());
            outboundPacket.writeInt(2);
            outboundPacket.writeBlockPos(posStart);
            outboundPacket.writeInt(1);
            outboundPacket.writeBlockPos(posEnd);
            railForward.writePacket(outboundPacket);
            outboundPacket.writeBlockPos(posEnd);
            outboundPacket.writeInt(1);
            outboundPacket.writeBlockPos(posStart);
            railBackward.writePacket(outboundPacket);

            if (outboundPacket.readableBytes() <= IPacket.MAX_PACKET_BYTES) {
                for (ServerPlayer levelPlayer : level.players()) {
                    Registry.sendToPlayer(levelPlayer, IPacket.PACKET_WRITE_RAILS, outboundPacket);
                }
            }
        });
    }
}
