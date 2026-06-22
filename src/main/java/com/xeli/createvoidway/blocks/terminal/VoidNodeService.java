package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.networking.packets.VoidNodeListPacket;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class VoidNodeService {

	private VoidNodeService() {
	}

	public static void sendNodeList(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (terminal.getLevel() instanceof ServerLevel level) {
			List<VoidNodeEntry> nodes = VoidNodeDiscovery.listNodes(level, terminal.getNetworkKey(), terminal.getBlockPos());
			PacketDistributor.sendToPlayer(player, new VoidNodeListPacket(terminal.getBlockPos(), nodes));
		}
	}

	public static boolean renameNode(ServerPlayer player, BlockPos terminalPos, ResourceLocation targetDimension,
			BlockPos targetPos, String newName) {
		ServerLevel terminalLevel = player.serverLevel();
		if (!(terminalLevel.getBlockEntity(terminalPos) instanceof VoidNodeTerminalTileEntity terminal))
			return false;
		if (!terminal.canOperate() || !terminal.getLink().canInteract(player))
			return false;
		if (!isSameNetwork(player.server, terminal.getNetworkKey(), targetDimension, targetPos))
			return false;

		VoidwayMod.VOID_NODE_NAMES_DATA.setName(targetDimension, targetPos, newName);
		sendNodeList(player, terminal);
		return true;
	}

	public static boolean teleportPlayer(ServerPlayer player, BlockPos terminalPos, ResourceLocation targetDimension,
			BlockPos targetPos) {
		ServerLevel terminalLevel = player.serverLevel();
		if (!(terminalLevel.getBlockEntity(terminalPos) instanceof VoidNodeTerminalTileEntity terminal))
			return false;
		if (!terminal.canOperate() || !terminal.getLink().canInteract(player))
			return false;

		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminalLevel);
		if (terminalDimension.equals(targetDimension) && terminalPos.equals(targetPos)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.cannot_teleport_self"), true);
			return false;
		}
		if (!isSameNetwork(player.server, terminal.getNetworkKey(), targetDimension, targetPos))
			return false;

		ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (targetLevel == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.dimension_unavailable"), true);
			return false;
		}

		int cost = VoidwayConfig.getVoidNodeTerminalTeleportFluidCostMb();
		if (terminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		terminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

		BlockState targetState = targetLevel.getBlockState(targetPos);
		if (!targetState.is(RWBlocks.VOID_NODE_TERMINAL.get())) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		Vec3 target = VoidwaySableCompat.globalTeleportPos(targetLevel, targetPos, 2.05);
		player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Collections.emptySet(),
				player.getYRot(), player.getXRot());
		VoidwaySableCompat.inheritSubLevelVelocity(targetLevel, player, target);
		targetLevel.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	private static boolean isSameNetwork(net.minecraft.server.MinecraftServer server, NetworkKey terminalKey,
			ResourceLocation targetDimension, BlockPos targetPos) {
		ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (level == null)
			return false;
		VoidLinkBehaviour targetLink = VoidNodeDiscovery.resolveLink(level, targetPos);
		if (targetLink == null)
			return false;
		if (!targetLink.getNetworkKey().equals(terminalKey))
			return false;
		if (targetLink.getFrequencyStack(true).isEmpty() || targetLink.getFrequencyStack(false).isEmpty())
			return false;
		return true;
	}

}
