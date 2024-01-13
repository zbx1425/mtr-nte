package cn.zbx1425.mtrsteamloco.forge;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.RegistriesWrapper;
import mtr.RegistryObject;
import mtr.item.ItemWithCreativeTabBase;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
#if MC_VERSION >= "12000" import net.minecraftforge.event.BuildCreativeModeTabContentsEvent; #endif
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistriesWrapperImpl implements RegistriesWrapper {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MOD_ID);
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Main.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MOD_ID);

    @Override
    public void registerBlock(String id, RegistryObject<Block> block) {
        BLOCKS.register(id, block::get);
    }

    @Override
    public void registerBlockAndItem(String id, RegistryObject<Block> block, #if MC_VERSION >= "12000" ResourceKey<CreativeModeTab> #else CreativeModeTab #endif tab) {
        BLOCKS.register(id, block::get);
        ITEMS.register(id, () -> {
            final BlockItem blockItem = new BlockItem(block.get(), RegistryUtilities.createItemProperties());
            registerCreativeModeTab(tab, blockItem);
            return blockItem;
        });
    }

    @Override
    public void registerItem(String id, RegistryObject<ItemWithCreativeTabBase> item) {
        ITEMS.register(id, () -> {
            final ItemWithCreativeTabBase itemObject = item.get();
            registerCreativeModeTab(itemObject.creativeModeTab.get(), itemObject);
            return itemObject;
        });
    }

    @Override
    public void registerBlockEntityType(String id, RegistryObject<? extends BlockEntityType<? extends BlockEntity>> blockEntityType) {
        BLOCK_ENTITY_TYPES.register(id, blockEntityType::get);
    }

    @Override
    public void registerEntityType(String id, RegistryObject<? extends EntityType<? extends Entity>> entityType) {
        ENTITY_TYPES.register(id, entityType::get);
    }

    @Override
    public void registerSoundEvent(String id, SoundEvent soundEvent) {
        SOUND_EVENTS.register(id, () -> soundEvent);
    }


    public final List<KeyMapping> keyMappings = new ArrayList<>();

    public void registerAllDeferred() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }


    private static final Map<#if MC_VERSION >= "12000" ResourceKey<CreativeModeTab> #else CreativeModeTab #endif, ArrayList<Item>> CREATIVE_TABS = new HashMap<>();

    public static void registerCreativeModeTab(#if MC_VERSION >= "12000" ResourceKey<CreativeModeTab> #else CreativeModeTab #endif resourceLocation, Item item) {
        CREATIVE_TABS.computeIfAbsent(resourceLocation, ignored -> new ArrayList<>()).add(item);
    }

    public static class RegisterCreativeTabs {

#if MC_VERSION >= "12000"
        @SubscribeEvent
        public static void onRegisterCreativeModeTabsEvent(BuildCreativeModeTabContentsEvent event) {
            CREATIVE_TABS.forEach((key, items) -> {
                if (event.getTabKey().equals(key)) {
                    items.forEach(item -> event.getEntries().put(new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
                }
            });
        }
#endif

    }
}