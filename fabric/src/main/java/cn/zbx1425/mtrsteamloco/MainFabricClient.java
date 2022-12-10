package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import cn.zbx1425.mtrsteamloco.render.RenderUtil;
import cn.zbx1425.mtrsteamloco.render.train.SteamSmokeParticle;
import cn.zbx1425.sowcerext.model.RawModel;
import cn.zbx1425.sowcerext.model.loader.NmbModelLoader;
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
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.apache.commons.io.FilenameUtils;
import net.minecraft.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
											String info = RenderUtil.getRenderStatusMessage();
#if MC_VERSION >= "11900"
											Minecraft.getInstance().player.sendSystemMessage(Text.literal(info));
#else
											Minecraft.getInstance().player.sendMessage(Text.literal(info), Util.NIL_UUID);
#endif
										});
										return 1;
									}))
#if DEBUG
							.then(ClientCommandManager.literal("exportmodels")
								.executes(context -> {
										for (Map.Entry<ResourceLocation, RawModel> pair : MainClient.modelManager.loadedRawModels.entrySet()) {
											Path path = Paths.get(FabricLoader.getInstance().getGameDir().toString(), "mtr-nte-models", pair.getKey().getNamespace(), pair.getKey().getPath());
											try {
												Files.createDirectories(path.getParent());
												FileOutputStream fos = new FileOutputStream(FilenameUtils.removeExtension(path.toString()) + ".nmb");
												NmbModelLoader.serializeModel(pair.getValue(), fos, false);
												fos.close();
											} catch (IOException e) {
												Main.LOGGER.error("Failed exporting models:", e);
											}
										}
										return 1;
									}))
#endif
			);

#if MC_VERSION >= "11900"
		});
#endif

		MainClient.init();
	}

}