package cn.zbx1425.mtrsteamloco.block;

import cn.zbx1425.mtrsteamloco.Main;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import mtr.block.IBlock;
import mtr.mappings.BlockDirectionalMapper;
import mtr.mappings.BlockEntityClientSerializableMapper;
import mtr.mappings.BlockEntityMapper;
import mtr.mappings.EntityBlockMapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.StringUtils;

public class BlockFeedbackBox extends BlockDirectionalMapper implements EntityBlockMapper {

    public BlockFeedbackBox() {
        super(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY).strength(2));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext collisionContext) {
        return IBlock.getVoxelShapeByDirection(2, 0, 0, 12, 13, 4, IBlock.getStatePropertySafe(state, FACING));
    }

    public final static String DUMMY_BOOK_IDENTIFY_TAG = "ZBX_FEEDBACK_DUMMY_BOOK";

    @Override
    public BlockEntityMapper createBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BlockEntityFeedbackBox(blockPos, blockState);
    }


    public static class BlockEntityFeedbackBox extends BlockEntityClientSerializableMapper {

        public String counterName;

        public BlockEntityFeedbackBox(BlockPos pos, BlockState state) {
            super(Main.BLOCK_ENTITY_TYPE_FEEDBACK_BOX.get(), pos, state);
        }

        @Override
        public void readCompoundTag(CompoundTag compoundTag) {
            counterName = compoundTag.getString("counterName");
            if (StringUtils.isEmpty(counterName)) counterName = null;
        }

        @Override
        public void writeCompoundTag(CompoundTag compoundTag) {
            compoundTag.putString("counterName", counterName == null ? "" : counterName);
        }
    }

    public static class ClientFunctions {

        public static void openDummyBookEditScreen(Player player, String counterName) {
            ItemStack dummyBookItemStack = new ItemStack(Items.WRITABLE_BOOK);
            dummyBookItemStack.addTagElement("counterName", StringTag.valueOf(counterName));
            dummyBookItemStack.addTagElement(DUMMY_BOOK_IDENTIFY_TAG, IntTag.valueOf(0));
            BookEditScreen bookEditScreen = new BookEditScreen(player, dummyBookItemStack, InteractionHand.MAIN_HAND);
            Minecraft.getInstance().setScreen(bookEditScreen);
        }
    }
}
