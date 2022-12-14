package cn.zbx1425.mtrsteamloco.network;

import cn.zbx1425.mtrsteamloco.Main;
import cn.zbx1425.mtrsteamloco.block.BlockEyeCandy;
import io.netty.buffer.Unpooled;
import mtr.RegistryClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PacketUpdateBlockEntity {

    public static ResourceLocation PACKET_UPDATE = new ResourceLocation(Main.MOD_ID, "update_block_entity");

    public static void sendUpdateC2S(BlockEyeCandy.BlockEntityEyeCandy blockEntity) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        final FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeResourceKey(level.dimension());
        packet.writeBlockPos(blockEntity.getBlockPos());
        packet.writeId(Registry.BLOCK_ENTITY_TYPE, blockEntity.getType());
        CompoundTag tag = new CompoundTag();
        blockEntity.writeCompoundTag(tag);
        packet.writeNbt(tag);

        RegistryClient.sendToServer(PACKET_UPDATE, packet);
    }

    public static void receiveUpdateC2S(MinecraftServer server, ServerPlayer player, FriendlyByteBuf packet) {
        ResourceKey<Level> levelKey = packet.readResourceKey(Registry.DIMENSION_REGISTRY);
        BlockPos blockPos = packet.readBlockPos();
        BlockEntityType<?> blockEntityType = packet.readById(Registry.BLOCK_ENTITY_TYPE);
        CompoundTag compoundTag = packet.readNbt();

        server.execute(() -> {
            ServerLevel level = server.getLevel(levelKey);
            if (level == null || blockEntityType == null) return;
            level.getBlockEntity(blockPos, blockEntityType).ifPresent(blockEntity -> {
                if (compoundTag != null) {
                    blockEntity.load(compoundTag);
                    blockEntity.setChanged();
                    level.getChunkSource().blockChanged(blockPos);
                }
            });
        });
    }
}
