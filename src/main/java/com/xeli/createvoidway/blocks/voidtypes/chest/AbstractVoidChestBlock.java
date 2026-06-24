package com.xeli.createvoidway.blocks.voidtypes.chest;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.VoidMachineBlockInteraction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class AbstractVoidChestBlock<T extends AbstractVoidChestTileEntity> extends HorizontalDirectionalBlock
		implements SimpleWaterloggedBlock, IBE<T>, IRotate {

	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

	protected AbstractVoidChestBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}

	protected abstract MapCodec<? extends HorizontalDirectionalBlock> codecImpl();

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return codecImpl();
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
		Direction facing = context.getHorizontalDirection().getOpposite();
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		return defaultBlockState()
				.setValue(FACING, facing)
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer,
			net.minecraft.world.item.ItemStack stack) {
		if (!level.isClientSide()) {
			VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(level, pos, VoidLinkBehaviour.TYPE);
			if (behaviour != null && placer instanceof Player player)
				behaviour.setOwner(player.getGameProfile());
		}
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		return VoidMachineBlockInteraction.useItemOnMachine(player, hit, level, pos,
				() -> useWithoutItem(state, level, pos, player, hit));
	}

	@Override
	public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide)
			return net.minecraft.world.InteractionResult.SUCCESS;

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof AbstractVoidChestTileEntity chest))
			return net.minecraft.world.InteractionResult.PASS;

		if (!chest.hasRequiredStress()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_chest.insufficient_stress"), true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (!chest.hasSufficientTransferFluid()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_chest.insufficient_transfer_fluid"),
					true);
			return net.minecraft.world.InteractionResult.FAIL;
		}

		if (player instanceof ServerPlayer serverPlayer)
			serverPlayer.openMenu(chest, chest::sendToMenu);
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

	@Override
	public abstract Class<T> getBlockEntityClass();

	@Override
	public abstract BlockEntityType<? extends T> getBlockEntityType();

}
