package cn.zbx1425.mtrsteamloco;

import mtr.RegistryObject;
import mtr.mappings.BlockEntityMapper;
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
		Registry.register(Registry.PARTICLE_TYPE, new ResourceLocation(Main.MOD_ID, "steam_smoke"), Main.PARTICLE_STEAM_SMOKE);
		Main.init(MainFabric::registerBlock, MainFabric::registerBlockEntityType, MainFabric::registerSoundEvent);
	}

	private static void registerBlock(String path, RegistryObject<Block> block, CreativeModeTab itemGroup) {
		Registry.register(Registry.BLOCK, new ResourceLocation(Main.MOD_ID, path), block.get());
		Registry.register(Registry.ITEM, new ResourceLocation(Main.MOD_ID, path), new BlockItem(block.get(), new Item.Properties().tab(itemGroup)));
	}

	private static <T extends BlockEntityMapper> void registerBlockEntityType(String path, RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>> blockEntityType) {
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation(Main.MOD_ID, path), blockEntityType.get());
	}

	private static void registerSoundEvent(String path, SoundEvent soundEvent) {
		Registry.register(Registry.SOUND_EVENT, new ResourceLocation(Main.MOD_ID, path), soundEvent);
	}
}
