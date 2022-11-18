package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.train.SteamSmokeParticle;
import mtr.mappings.Text;
import net.fabricmc.api.ClientModInitializer;
#if MC_VERSION >= "11900"
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
#else
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
#endif
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class MainFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(((atlasTexture, registry) -> {
			registry.register(new ResourceLocation(Main.MOD_ID, "particle/steam_smoke"));
		}));

		ParticleFactoryRegistry.getInstance().register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);

#if MC_VERSION >= "11900"
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
#else
		ClientCommandManager.DISPATCHER.register(
#endif

					ClientCommandManager.literal("mtrnte")
							.then(ClientCommandManager.literal("config")
									.executes(context -> {
										Minecraft.getInstance().tell(() -> {
											Minecraft.getInstance().setScreen(new ConfigScreen(Minecraft.getInstance().screen));
										});
										return 1;
									}))
							.then(ClientCommandManager.literal("stat")
									.executes(context -> {
										Minecraft.getInstance().tell(() -> {
											String info = "[NTE Sowcer] Draw Calls: " + MainClient.batchManager.drawCallCount
													+ ", Batches: " + MainClient.batchManager.batchCount
													+ ", Faces: " + MainClient.batchManager.faceCount;
#if MC_VERSION >= "11900"
											Minecraft.getInstance().player.sendSystemMessage(Text.literal(info));
#else
											Minecraft.getInstance().player.sendMessage(Text.literal(info), Util.NIL_UUID);
#endif
										});
										return 1;
									}))
			);

#if MC_VERSION >= "11900"
		});
#endif


		MainClient.init();
	}

}