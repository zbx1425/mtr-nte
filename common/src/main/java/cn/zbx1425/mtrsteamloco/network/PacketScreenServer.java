package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.Main;
import io.netty.buffer.Unpooled;
import mtr.Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PacketScreenServer {

    public static ResourceLocation PACKET_SHOW_SCREEN = new ResourceLocation(Main.MOD_ID, "show_screen");

    public static void sendBlockEntityScreenS2C(ServerPlayer player, String screenName, BlockPos pos) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(screenName);
        packet.writeBlockPos(pos);
        Registry.sendToPlayer(player, PACKET_SHOW_SCREEN, packet);
    }
}
