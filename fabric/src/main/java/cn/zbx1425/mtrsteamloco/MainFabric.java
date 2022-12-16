package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.mappings.FabricRegistryUtilities;
import mtr.CreativeModeTabs;
import mtr.RegistryObject;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.RegistryUtilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		Main.PARTICLE_STEAM_SMOKE = FabricParticleTypes.simple(true);
		Registry.register(RegistryUtilities.registryGetParticleType(), new ResourceLocation(Main.MOD_ID, "steam_smoke"), Main.PARTICLE_STEAM_SMOKE);
		Main.init(MainFabric::registerBlock, MainFabric::registerBlockEntityType, MainFabric::registerSoundEvent);
	}

	private static void registerBlock(String path, RegistryObject<Block> block, CreativeModeTabs.Wrapper creativeModeTab) {
		Registry.register(RegistryUtilities.registryGetBlock(), new ResourceLocation(Main.MOD_ID, path), block.get());
		final BlockItem blockItem = new BlockItem(block.get(), RegistryUtilities.createItemProperties(creativeModeTab::get));
		Registry.register(RegistryUtilities.registryGetItem(), new ResourceLocation(Main.MOD_ID, path), blockItem);
		FabricRegistryUtilities.registerCreativeModeTab(creativeModeTab.get(), blockItem);
	}

	private static void registerBlockEntityType(String path, RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>> blockEntityType) {
		Registry.register(RegistryUtilities.registryGetBlockEntityType(), new ResourceLocation(Main.MOD_ID, path), blockEntityType.get());
	}

	private static void registerSoundEvent(String path, SoundEvent soundEvent) {
		Registry.register(RegistryUtilities.registryGetSoundEvent(), new ResourceLocation(Main.MOD_ID, path), soundEvent);
	}
}
