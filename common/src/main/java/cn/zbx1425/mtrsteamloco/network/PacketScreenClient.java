package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.gui.EyeCandyScreen;
import mtr.mappings.UtilitiesClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class PacketScreenClient {

    public static void receiveBlockEntityScreenS2C(FriendlyByteBuf packet) {
        Minecraft minecraftClient = Minecraft.getInstance();
        String screenName = packet.readUtf();
        BlockPos pos = packet.readBlockPos();
        minecraftClient.execute(() -> {
            switch (screenName) {
                case "eye_candy":
                    if (!(minecraftClient.screen instanceof EyeCandyScreen)) {
                        UtilitiesClient.setScreen(minecraftClient, new EyeCandyScreen(pos));
                    }
                    break;
            }
        });

    }
}
