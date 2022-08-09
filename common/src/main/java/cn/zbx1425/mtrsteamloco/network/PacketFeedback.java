package cn.zbx1425.mtrsteamloco.network;

import io.netty.buffer.Unpooled;
import mtr.Registry;
import mtr.RegistryClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PacketFeedback {

    public static final ResourceLocation PACKET_FEEDBACK = new ResourceLocation("mtrsteamloco", "packet_feedback");

    public static void sendFeedbackC2S(String counterName, String content) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(counterName);
        packet.writeUtf(content);
        RegistryClient.sendToServer(PACKET_FEEDBACK, packet);
    }

    public static void receiveFeedbackC2S(MinecraftServer minecraftServer, ServerPlayer player, FriendlyByteBuf packet) {
        String counterName = packet.readUtf();
        String content = packet.readUtf();
        RequestFactory.buildFeedback(counterName, player, content, url -> sendFeedbackS2C(player, url)).sendAsync();
    }

    public static void sendFeedbackS2C(ServerPlayer player, String url) {
        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeUtf(url);
        Registry.sendToPlayer(player, PACKET_FEEDBACK, packet);
    }

    public static void receiveFeedbackS2C(FriendlyByteBuf packet) {
        String url = packet.readUtf();
        if (Minecraft.getInstance().isRunning()) {
            TranslatableComponent chatComponent = new TranslatableComponent("gui.mtrsteamloco.feedback_success");
            TextComponent urlComponent = new TextComponent(url);
            ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            urlComponent.setStyle(urlComponent.getStyle().withClickEvent(click).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            chatComponent.append(urlComponent);
            Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(chatComponent));
        }
    }
}
