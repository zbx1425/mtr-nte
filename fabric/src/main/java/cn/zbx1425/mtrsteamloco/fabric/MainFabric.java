package cn.zbx1425.mtrsteamloco.fabric;

import cn.zbx1425.mtrsteamloco.Main;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class MainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		Main.PARTICLE_STEAM_SMOKE = FabricParticleTypes.simple(true);
		Registry.register(RegistryUtilities.registryGetParticleType(), new ResourceLocation(Main.MOD_ID, "steam_smoke"), Main.PARTICLE_STEAM_SMOKE);
		Main.init(new RegistriesWrapperImpl());
	}
}
