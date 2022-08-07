package cn.zbx1425.mtrsteamloco;

import cn.zbx1425.mtrsteamloco.block.BlockDepartureBell;
import cn.zbx1425.mtrsteamloco.block.BlockStatisticTurnstile;
import mtr.ItemGroups;
import mtr.RegistryObject;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.RegistryUtilities;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;

public class Main {

	public static final String MOD_ID = "mtrsteamloco";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static final RegistryObject<Block> BLOCK_DEPARTURE_BELL = new RegistryObject<>(BlockDepartureBell::new);

	public static final RegistryObject<Block> BLOCK_STATISTIC_TURNSTILE = new RegistryObject<>(BlockStatisticTurnstile::new);
	public static final SoundEvent SOUND_EVENT_BELL = new SoundEvent(new ResourceLocation("mtrsteamloco:bell"));

	public static final RegistryObject<BlockEntityType<BlockStatisticTurnstile.BlockEntityStatisticTurnstile>>
			BLOCK_ENTITY_TYPE_STATISTIC_TURNSTILE = new RegistryObject<>(() ->
			RegistryUtilities.getBlockEntityType(
					BlockStatisticTurnstile.BlockEntityStatisticTurnstile::new,
					BLOCK_STATISTIC_TURNSTILE.get()
			));

	public static SimpleParticleType PARTICLE_STEAM_SMOKE;

	public static void init(
			RegisterBlockItem registerBlockItem,
			BiConsumer<String,RegistryObject<? extends BlockEntityType<? extends BlockEntityMapper>>> registerBlockEntityType,
			BiConsumer<String, SoundEvent> registerSoundEvent
	) {
		registerBlockItem.accept("departure_bell", BLOCK_DEPARTURE_BELL, ItemGroups.RAILWAY_FACILITIES);
		registerBlockItem.accept("statistic_turnstile", BLOCK_STATISTIC_TURNSTILE, ItemGroups.RAILWAY_FACILITIES);
		registerBlockEntityType.accept("statistic_turnstile", BLOCK_ENTITY_TYPE_STATISTIC_TURNSTILE);
		registerSoundEvent.accept("bell", SOUND_EVENT_BELL);
	}

	@FunctionalInterface
	public interface RegisterBlockItem {
		void accept(String string, RegistryObject<Block> block, CreativeModeTab tab);
	}
}
