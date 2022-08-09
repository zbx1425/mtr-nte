package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.SteamSmokeParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy {

    public static void initClient() {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((mc, screen) -> new ConfigScreen(screen)));
    }


    public static class ModEventBusListener {

        @SubscribeEvent
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            MainClient.init();
        }

        @SubscribeEvent
        public static void onRegistryParticleFactory(ParticleFactoryRegisterEvent event) {
            Minecraft.getInstance().particleEngine.register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onDebugOverlay(RenderGameOverlayEvent.Text event) {
            if (Minecraft.getInstance().options.renderDebug) {
                event.getLeft().add(
                        "[MTRSteamLoco] Draw Calls: " + MainClient.batchManager.drawCallCount
                                + ", Batches: " + MainClient.batchManager.batchCount
                );
            }
        }

        @SubscribeEvent
        public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
            event.getDispatcher().register(Commands.literal("mtrsteamloco")
                    .then(Commands.literal("config")
                            .executes(context -> {
                                Minecraft.getInstance().tell(() -> {
                                    Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen));
                                });
                                return 1;
                            }))
            );
        }
    }
}
