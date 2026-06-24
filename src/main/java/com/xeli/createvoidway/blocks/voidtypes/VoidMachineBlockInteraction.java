package com.xeli.createvoidway.blocks.voidtypes;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.terminal.VoidNodeTerminalMultiblock;
import com.xeli.createvoidway.voidlink.VoidLinkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

public final class VoidMachineBlockInteraction {

	private VoidMachineBlockInteraction() {
	}

	public static ItemInteractionResult useItemOnMachine(Player player, BlockHitResult hit, Level level, BlockPos pos,
			Supplier<InteractionResult> machineUse) {
		if (player.isShiftKeyDown())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if (hitsLinkSlot(level, pos, hit))
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		InteractionResult result = machineUse.get();
		if (result != InteractionResult.PASS)
			return ItemInteractionResult.SUCCESS;
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public static boolean hitsLinkSlot(Level level, BlockPos pos, BlockHitResult hit) {
		BlockPos linkPos = VoidNodeTerminalMultiblock.resolveBehaviourPos(level, pos);
		VoidLinkBehaviour link = BlockEntityBehaviour.get(level, linkPos, VoidLinkBehaviour.TYPE);
		if (link == null)
			return false;
		for (int index : VoidLinkHandler.arr012) {
			if (link.testHit(index, hit.getLocation()))
				return true;
		}
		return false;
	}

}
