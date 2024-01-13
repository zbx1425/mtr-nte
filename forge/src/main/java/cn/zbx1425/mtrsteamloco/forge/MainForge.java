package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.gui.ConfigScreen;
import mtr.CreativeModeTabs;
import mtr.Registry;
import mtr.RegistryObject;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.DeferredRegisterHolder;
import cn.zbx1425.mtrsteamloco.mappings.ForgeUtilities;
import mtr.mappings.RegistryUtilities;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
#if MC_VERSION >= "11900"
import net.minecraftforge.client.ConfigScreenHandler;
#elif MC_VERSION >= "11800"
import net.minecraftforge.client.ConfigGuiHandler;
#else
import net.minecraftforge.fmlclient.ConfigGuiHandler;
#endif
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MOD_ID)
public class MainForge {


	private static final DeferredRegisterHolder<Item> ITEMS = new DeferredRegisterHolder<>(Main.MOD_ID, ForgeUtilities.registryGetItem());
	private static final DeferredRegisterHolder<Block> BLOCKS = new DeferredRegisterHolder<>(Main.MOD_ID, ForgeUtilities.registryGetBlock());
	private static final DeferredRegisterHolder<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new DeferredRegisterHolder<>(Main.MOD_ID, ForgeUtilities.registryGetBlockEntityType());
	private static final DeferredRegisterHolder<SoundEvent> SOUND_EVENTS = new DeferredRegisterHolder<>(Main.MOD_ID, ForgeUtilities.registryGetSoundEvent());

	private static final DeferredRegisterHolder<ParticleType<?>> PARTICLE_TYPES = new DeferredRegisterHolder<>(Main.MOD_ID, ForgeUtilities.registryGetParticleType());

	static {
		Main.init(new RegistriesWrapperImpl());
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
			eventBus.register(ClientProxy.ModEventBusListener.class);
			MinecraftForge.EVENT_BUS.register(ClientProxy.ForgeEventBusListener.class);
		});

#if MC_VERSION >= "11900"
		ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
				() -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, parent) ->
						ConfigScreen.createScreen(parent))));
#else
		ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
				() -> new ConfigGuiHandler.ConfigGuiFactory(((minecraft, parent) ->
						ConfigScreen.createScreen(parent))));
#endif
	}
	private static void registerBlock(String path, RegistryObject<Block> block) {
		BLOCKS.register(path, block::get);
	}

	private static void registerBlock(String path, RegistryObject<Block> block, CreativeModeTabs.Wrapper creativeModeTabWrapper) {
		registerBlock(path, block);
		ITEMS.register(path, () -> {
			final BlockItem blockItem = new BlockItem(block.get(), RegistryUtilities.createItemProperties(creativeModeTabWrapper::get));
			Registry.registerCreativeModeTab(creativeModeTabWrapper.resourceLocation, blockItem);
			return blockItem;
		});
	}

	private static void registerBlockEntityType(String path, RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>> blockEntityType) {
		BLOCK_ENTITY_TYPES.register(path, blockEntityType::get);
	}

	private static void registerSoundEvent(String path, SoundEvent soundEvent) {
		SOUND_EVENTS.register(path, () -> soundEvent);
	}

}
