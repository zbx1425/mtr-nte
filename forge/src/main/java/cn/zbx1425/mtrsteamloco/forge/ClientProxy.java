package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.ClientConfig;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.train.SteamSmokeParticle;
import mtr.mappings.Text;
import net.minecraft.client.Minecraft;
#if MC_VERSION >= "11900"
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
#else
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.Util;
#endif
import net.minecraft.commands.Commands;
#if MC_VERSION >= "11800"
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
#endif
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy {

    public static void initClient() {

    }


    public static class ModEventBusListener {

        @SubscribeEvent
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            MainClient.init();
        }

        @SubscribeEvent
#if MC_VERSION >= "11900"
        public static void onRegistryParticleFactory(RegisterParticleProvidersEvent event) {
#else
        public static void onRegistryParticleFactory(ParticleFactoryRegisterEvent event) {
#endif
            Minecraft.getInstance().particleEngine.register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
#if MC_VERSION >= "11900"
        public static void onDebugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
#else
        public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
#endif
            if (Minecraft.getInstance().options.renderDebug) {
                event.getLeft().add(
                        "[NTE] Calls: " + MainClient.drawContext.drawCallCount
                                + ", Batches: " + MainClient.drawContext.batchCount
                                + ", Faces: " + (MainClient.drawContext.singleFaceCount + MainClient.drawContext.instancedFaceCount)
                );
            }
        }
#if MC_VERSION >= "11800"
        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("mtrnte")
                            .then(Commands.literal("config")
                                    .executes(context -> {
                                        Minecraft.getInstance().tell(() -> {
                                            Minecraft.getInstance().setScreen(ConfigScreen.createScreen(Minecraft.getInstance().screen));
                                        });
                                        return 1;
                                    }))
                            .then(Commands.literal("hideriding")
                                    .executes(context -> {
                                        ClientConfig.hideRidingTrain = !ClientConfig.hideRidingTrain;
                                        return 1;
                                    }))
                            .then(Commands.literal("stat")
                                    .executes(context -> {
                                        Minecraft.getInstance().tell(() -> {
                                            String info = RenderUtil.getRenderStatusMessage();
#if MC_VERSION >= "11900"
                                            Minecraft.getInstance().player.sendSystemMessage(Text.literal(info));
#else
											Minecraft.getInstance().player.sendMessage(Text.literal(info), Util.NIL_UUID);
#endif
                                        });
                                        return 1;
                                    }))
            );
        }
#endif
    }
}