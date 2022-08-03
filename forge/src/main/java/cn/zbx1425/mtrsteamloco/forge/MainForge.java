package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.MainClient;
import cn.zbx1425.mtrsteamloco.render.SteamSmokeParticle;
import mtr.RegistryObject;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.DeferredRegisterHolder;
import cn.zbx1425.mtrsteamloco.mappings.ForgeUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MOD_ID)
public class MainForge {

	private static final DeferredRegisterHolder<Item> ITEMS = new DeferredRegisterHolder<>(Main.MOD_ID, Registry.ITEM_REGISTRY);
	private static final DeferredRegisterHolder<Block> BLOCKS = new DeferredRegisterHolder<>(Main.MOD_ID, Registry.BLOCK_REGISTRY);
	private static final DeferredRegisterHolder<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new DeferredRegisterHolder<>(Main.MOD_ID, Registry.BLOCK_ENTITY_TYPE_REGISTRY);
	private static final DeferredRegisterHolder<SoundEvent> SOUND_EVENTS = new DeferredRegisterHolder<>(Main.MOD_ID, Registry.SOUND_EVENT_REGISTRY);

	private static final DeferredRegisterHolder<ParticleType<?>> PARTICLE_TYPES = new DeferredRegisterHolder<>(Main.MOD_ID, Registry.PARTICLE_TYPE_REGISTRY);

	static {
		Main.init(MainForge::registerBlock, MainForge::registerBlockEntityType, MainForge::registerSoundEvent);
	}

	public MainForge() {

		final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ForgeUtilities.registerModEventBus(Main.MOD_ID, eventBus);

		Main.PARTICLE_STEAM_SMOKE = new SimpleParticleType(true);
		PARTICLE_TYPES.register("steam_smoke", () -> Main.PARTICLE_STEAM_SMOKE);

		ITEMS.register();
		BLOCKS.register();
		BLOCK_ENTITY_TYPES.register();
		SOUND_EVENTS.register();
		PARTICLE_TYPES.register();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, RenderConfigForge.CONFIG_SPEC);
			ConfigScreen.register();
			eventBus.register(ModEventBusListener.class);
			MinecraftForge.EVENT_BUS.register(ForgeEventBusListener.class);
		});
	}

	private static void registerBlock(String path, RegistryObject<Block> block) {
		BLOCKS.register(path, block::register);
	}

	private static void registerBlock(String path, RegistryObject<Block> block, CreativeModeTab itemGroup) {
		registerBlock(path, block);
		ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties().tab(itemGroup)));
	}

	private static <T extends BlockEntityMapper> void registerBlockEntityType(String path, RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>>blockEntityType) {
		BLOCK_ENTITY_TYPES.register(path, blockEntityType::register);
	}

	private static void registerSoundEvent(String path, SoundEvent soundEvent) {
		SOUND_EVENTS.register(path, () -> soundEvent);
	}

	private static class ModEventBusListener {

		@SubscribeEvent
		public static void onClientSetupEvent(FMLClientSetupEvent event) {
			MainClient.init();
		}

		@SubscribeEvent
		public static void onConfigLoad(ModConfigEvent.Loading event) {
			RenderConfigForge.apply();
		}

		@SubscribeEvent
		public static void onRegistryParticleFactory(ParticleFactoryRegisterEvent event) {
			Minecraft.getInstance().particleEngine.register(Main.PARTICLE_STEAM_SMOKE, SteamSmokeParticle.Provider::new);
		}
	}

	private static class ForgeEventBusListener {

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
