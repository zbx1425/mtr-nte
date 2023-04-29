package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.BuildConfig;
import cn.zbx1425.mtrsteamloco.Main;
import io.netty.buffer.Unpooled;
import mtr.Registry;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PacketVersionCheck {

    public static final ResourceLocation PACKET_VERSION_CHECK = new ResourceLocation(Main.MOD_ID, "version_check");

    public static void sendVersionCheckS2C(ServerPlayer player) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(BuildConfig.MOD_VERSION.split("-hotfix-")[0]);
        Registry.sendToPlayer(player, PACKET_VERSION_CHECK, packet);
    }
    public static void receiveVersionCheckS2C(FriendlyByteBuf packet) {

    }
}
