package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.SteamSmokeParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
        public static void onRegistryParticleFactory(RegisterParticleProvidersEvent event) {
            Minecraft.getInstance().particleEngine.register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);
        }
    }

    public static class ForgeEventBusListener {

        @SubscribeEvent
        public static void onDebugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
            if (Minecraft.getInstance().options.renderDebug) {
                event.getLeft().add(
                        "[MTRSteamLoco] Draw Calls: " + MainClient.batchManager.drawCallCount
                                + ", Batches: " + MainClient.batchManager.batchCount
                );
            }
        }
    }
}