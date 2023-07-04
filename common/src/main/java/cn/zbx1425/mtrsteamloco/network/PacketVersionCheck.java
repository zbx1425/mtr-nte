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
        packet.writeUtf(BuildConfig.MOD_VERSION);
        packet.writeInt(BuildConfig.MOD_PROTOCOL_VERSION);
        Registry.sendToPlayer(player, PACKET_VERSION_CHECK, packet);
    }
    public static void receiveVersionCheckS2C(FriendlyByteBuf packet) {
        final String remoteVersion = packet.readUtf();
        final int remoteProtocolVersion;
        if (packet.readableBytes() < 4) {
            remoteProtocolVersion = 0;
        } else {
            remoteProtocolVersion = packet.readInt();
        }
        boolean protocolMatches = (remoteProtocolVersion == BuildConfig.MOD_PROTOCOL_VERSION)
            || (BuildConfig.MOD_PROTOCOL_VERSION == 1 && remoteProtocolVersion == 0 &&
                remoteVersion.split("-")[1].startsWith("0.3."));
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.execute(() -> {
            if (!protocolMatches) {
                final ClientPacketListener connection = minecraftClient.getConnection();
                String serverVersion = remoteVersion + " (" + remoteProtocolVersion + ")";
                String localVersion = BuildConfig.MOD_VERSION + " (" + BuildConfig.MOD_PROTOCOL_VERSION + ")";
                if (connection != null) {
                    final int widthDifference1 = minecraftClient.font.width(Text.translatable("gui.mtr.mismatched_versions_your_version")) - minecraftClient.font.width(Text.translatable("gui.mtr.mismatched_versions_server_version"));
                    final int widthDifference2 = minecraftClient.font.width(localVersion) - minecraftClient.font.width(serverVersion);
                    final int spaceWidth = minecraftClient.font.width(" ");

                    final StringBuilder text = new StringBuilder();
                    for (int i = 0; i < -widthDifference1 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append(Text.translatable("gui.mtr.mismatched_versions_your_version", localVersion).getString());
                    for (int i = 0; i < -widthDifference2 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append("\n");
                    for (int i = 0; i < widthDifference1 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append(Text.translatable("gui.mtr.mismatched_versions_server_version", serverVersion).getString());
                    for (int i = 0; i < widthDifference2 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append("\n\n");

                    connection.getConnection().disconnect(
                        Text.literal(text.toString())
                            .append(Text.literal(
                                    Text.translatable("gui.mtr.mismatched_versions").getString()
                                            .replace("Minecraft Transit Railway", "NTE (Nemo's Transit Expansion)")
                            ))
                    );
                }
            }
        });
    }
}
