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
        final String version = packet.readUtf();
        Minecraft minecraftClient = Minecraft.getInstance();
        minecraftClient.execute(() -> {
            if (!BuildConfig.MOD_VERSION.split("-hotfix-")[0].equals(version)) {
                final ClientPacketListener connection = minecraftClient.getConnection();
                if (connection != null) {
                    final int widthDifference1 = minecraftClient.font.width(Text.translatable("gui.mtr.mismatched_versions_your_version")) - minecraftClient.font.width(Text.translatable("gui.mtr.mismatched_versions_server_version"));
                    final int widthDifference2 = minecraftClient.font.width(BuildConfig.MOD_VERSION) - minecraftClient.font.width(version);
                    final int spaceWidth = minecraftClient.font.width(" ");

                    final StringBuilder text = new StringBuilder();
                    for (int i = 0; i < -widthDifference1 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append(Text.translatable("gui.mtr.mismatched_versions_your_version", BuildConfig.MOD_VERSION).getString());
                    for (int i = 0; i < -widthDifference2 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append("\n");
                    for (int i = 0; i < widthDifference1 / spaceWidth; i++) {
                        text.append(" ");
                    }
                    text.append(Text.translatable("gui.mtr.mismatched_versions_server_version", version).getString());
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
