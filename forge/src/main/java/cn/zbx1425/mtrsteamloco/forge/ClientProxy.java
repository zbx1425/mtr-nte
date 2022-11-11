package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.SteamSmokeParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
#if MC_VERSION >= "11900"
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
#else
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
#endif
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
                        "[MTRSteamLoco] Draw Calls: " + MainClient.batchManager.drawCallCount
                                + ", Batches: " + MainClient.batchManager.batchCount
                );
            }
        }
    }
}