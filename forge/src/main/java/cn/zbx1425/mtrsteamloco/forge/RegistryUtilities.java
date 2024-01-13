package cn.zbx1425.mtrsteamloco.forge;


import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface RegistryUtilities {

    static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> callback) {
        CommandRegistrationEvent.EVENT.register((dispatcher, dedicated, commandSelection) -> callback.accept(dispatcher));
    }

    static void registerPlayerJoinEvent(Consumer<ServerPlayer> consumer) {
        PlayerEvent.PLAYER_JOIN.register(consumer::accept);
    }

    static void registerPlayerQuitEvent(Consumer<ServerPlayer> consumer) {
        PlayerEvent.PLAYER_QUIT.register(consumer::accept);
    }

    static void registerPlayerChangeDimensionEvent(Consumer<ServerPlayer> consumer) {
        PlayerEvent.CHANGE_DIMENSION.register(((player, oldWorld, newWorld) -> consumer.accept(player)));
    }

    static void registerServerStartingEvent(Consumer<MinecraftServer> consumer) {
        LifecycleEvent.SERVER_STARTING.register(consumer::accept);
    }

    static void registerServerStoppingEvent(Consumer<MinecraftServer> consumer) {
        LifecycleEvent.SERVER_STOPPING.register(consumer::accept);
    }

    static void registerTickEvent(Consumer<MinecraftServer> consumer) {
        TickEvent.SERVER_PRE.register(consumer::accept);
    }

    static Item.Properties createItemProperties() {
        return new Item.Properties();
    }
}