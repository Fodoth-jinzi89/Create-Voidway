package com.xeli.createvoidway.blocks.terminal;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.VoidMachineBlockInteraction;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.voidlink.VoidLinkHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class VoidNodeTerminalBlock extends HorizontalDirectionalBlock
		implements SimpleWaterloggedBlock, IBE<VoidNodeTerminalTileEntity>, IRotate {

	public static final MapCodec<VoidNodeTerminalBlock> CODEC = simpleCodec(VoidNodeTerminalBlock::new);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

	public VoidNodeTerminalBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED))
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED, FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		if (!context.getLevel().getBlockState(pos.above()).canBeReplaced(context))
			return null;
		Direction facing = context.getHorizontalDirection().getOpposite();
		FluidState fluidState = context.getLevel().getFluidState(pos);
		return defaultBlockState()
				.setValue(FACING, facing)
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, level, pos, oldState, isMoving);
		if (!level.isClientSide)
			VoidNodeTerminalMultiblock.placeTop(level, pos, state.getValue(FACING));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide)
			VoidNodeTerminalMultiblock.removeTop(level, pos);
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
			net.minecraft.world.item.ItemStack stack) {
		if (!level.isClientSide()) {
			VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
			if (behaviour != null && placer instanceof net.minecraft.world.entity.player.Player player)
				behaviour.setOwner(player.getGameProfile());
		}
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isShiftKeyDown() && heldItem.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos basePos = resolveLinkPos(level, pos);
		return VoidMachineBlockInteraction.useItemOnMachine(player, hit, level, basePos,
				() -> useWithoutItem(level.getBlockState(basePos), level, basePos, player, hit));
	}

	@Override
	public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			net.minecraft.world.entity.player.Player player, BlockHitResult hit) {
		BlockPos linkPos = resolveLinkPos(level, pos);
		VoidLinkBehaviour link = BlockEntityBehaviour.get(level, linkPos, VoidLinkBehaviour.TYPE);
		if (link != null) {
			for (int index : VoidLinkHandler.arr012) {
				if (link.testHit(index, hit.getLocation()))
					return net.minecraft.world.InteractionResult.SUCCESS;
			}
		}

		if (level.isClientSide)
			return net.minecraft.world.InteractionResult.SUCCESS;

		BlockPos basePos = resolveLinkPos(level, pos);
		BlockEntity blockEntity = level.getBlockEntity(basePos);
		if (!(blockEntity instanceof VoidNodeTerminalTileEntity terminal))
			return net.minecraft.world.InteractionResult.PASS;

		if (!terminal.hasRequiredStress()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_stress"), true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (!terminal.hasSufficientTransferFluid()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_transfer_fluid"), true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (terminal.getLink().getFrequencyStack(true).isEmpty() || terminal.getLink().getFrequencyStack(false).isEmpty()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.frequency_incomplete"), true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (terminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.teleport_cooldown"), true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (player instanceof ServerPlayer serverPlayer)
			serverPlayer.openMenu(terminal, terminal::sendToMenu);
		return net.minecraft.world.InteractionResult.SUCCESS;
	}

	@Override
	public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
		return face == Direction.DOWN;
	}

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	/** Face toward the player; matches south-baked slot models with fixed blockstate rotation. */
	public static Direction getLinkSlotFace(BlockState state) {
		return state.getValue(FACING);
	}

	private static BlockPos resolveLinkPos(Level level, BlockPos pos) {
		BlockPos base = VoidNodeTerminalMultiblock.getBasePos(level, pos);
		return base != null ? base : pos;
	}

	@Override
	public Class<VoidNodeTerminalTileEntity> getBlockEntityClass() {
		return VoidNodeTerminalTileEntity.class;
	}

	@Override
	public BlockEntityType<? extends VoidNodeTerminalTileEntity> getBlockEntityType() {
		return com.xeli.createvoidway.blocks.RWTileEntities.VOID_NODE_TERMINAL.get();
	}

}
