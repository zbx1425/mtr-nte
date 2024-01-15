package cn.zbx1425.mtrsteamloco.fabric;


#if MC_VERSION >= "12000"
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
#else
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
#endif
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import mtr.CreativeModeTabs;
import mtr.RegistryObject;
import mtr.item.ItemWithCreativeTabBase;
import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.RegistriesWrapper;
import mtr.mappings.RegistryUtilities;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class RegistriesWrapperImpl implements RegistriesWrapper {

    @Override
    public void registerBlock(String id, RegistryObject<Block> block) {
        Registry.register(RegistryUtilities.registryGetBlock(), new ResourceLocation(Main.MOD_ID, id), block.get());
    }

    @Override
    public void registerBlockAndItem(String id, RegistryObject<Block> block, CreativeModeTabs.Wrapper tab) {
        Registry.register(RegistryUtilities.registryGetBlock(), new ResourceLocation(Main.MOD_ID, id), block.get());
#if MC_VERSION >= "12000"
        final BlockItem blockItem = new BlockItem(block.get(), new Item.Properties());
#else
        final BlockItem blockItem = new BlockItem(block.get(), new FabricItemSettings().group(tab.get()));
#endif
        Registry.register(RegistryUtilities.registryGetItem(), new ResourceLocation(Main.MOD_ID, id), blockItem);
#if MC_VERSION >= "12000"
        ItemGroupEvents.modifyEntriesEvent(
                ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), tab.resourceLocation))
            .register(consumer -> consumer.accept(blockItem));
#endif
    }

    @Override
    public void registerItem(String id, RegistryObject<ItemWithCreativeTabBase> item) {
        Registry.register(RegistryUtilities.registryGetItem(), new ResourceLocation(Main.MOD_ID, id), item.get());
#if MC_VERSION >= "12000"
        ItemGroupEvents.modifyEntriesEvent(
                ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), item.get().creativeModeTab.resourceLocation))
            .register(consumer -> consumer.accept(item.get()));
#endif
    }

    @Override
    public void registerBlockEntityType(String id, RegistryObject<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        Registry.register(RegistryUtilities.registryGetBlockEntityType(), new ResourceLocation(Main.MOD_ID, id), blockEntityType.get());
    }

    @Override
    public void registerEntityType(String id, RegistryObject<? extends EntityType<? extends Entity>> entityType) {
        Registry.register(RegistryUtilities.registryGetEntityType(), new ResourceLocation(Main.MOD_ID, id), entityType.get());
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        Registry.register(RegistryUtilities.registryGetSoundEvent(), new ResourceLocation(Main.MOD_ID, id), soundEvent);
    }

    @Override
    public void registerParticleType(String id, ParticleType<?> particleType) {
        Registry.register(RegistryUtilities.registryGetParticleType(), new ResourceLocation(Main.MOD_ID, id), particleType);
    }

    @Override
    public SimpleParticleType createParticleType(boolean overrideLimiter) {
        return FabricParticleTypes.simple(overrideLimiter);
    }
}