package cn.zbx1425.mtrsteamloco.fabric;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
#if MC_VERSION >= "12000"
import net.minecraft.core.registries.BuiltInRegistries;
#endif
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;


public interface RegistryUtilities {

    static Item.Properties createItemProperties() {
        return new Item.Properties();
    }

    static DefaultedRegistry<Item> registryGetItem() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.ITEM #else Registry.ITEM #endif;
    }

    static DefaultedRegistry<Block> registryGetBlock() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.BLOCK #else Registry.BLOCK #endif;
    }

    static Registry<BlockEntityType<?>> registryGetBlockEntityType() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.BLOCK_ENTITY_TYPE #else Registry.BLOCK_ENTITY_TYPE #endif;
    }

    static DefaultedRegistry<EntityType<?>> registryGetEntityType() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.ENTITY_TYPE #else Registry.ENTITY_TYPE #endif;
    }

    static Registry<SoundEvent> registryGetSoundEvent() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.SOUND_EVENT #else Registry.SOUND_EVENT #endif;
    }

    static Registry<ParticleType<?>> registryGetParticleType() {
        return #if MC_VERSION >= "12000" BuiltInRegistries.PARTICLE_TYPE #else Registry.PARTICLE_TYPE #endif;
    }
}