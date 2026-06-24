package com.xeli.createvoidway.blocks.terminal;

import com.xeli.createvoidway.VoidwayMod;
import com.xeli.createvoidway.blocks.RWBlocks;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import com.xeli.createvoidway.config.VoidwayConfig;
import com.xeli.createvoidway.items.PortableVoidTerminalBinding;
import com.xeli.createvoidway.items.RWItems;
import com.xeli.createvoidway.networking.packets.PortableVoidTerminalListPacket;
import com.xeli.createvoidway.networking.packets.VoidNodeListPacket;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public final class VoidNodeService {

	private VoidNodeService() {
	}

	public static void sendNodeList(ServerPlayer player, VoidNodeTerminalTileEntity terminal) {
		if (terminal.getLevel() instanceof ServerLevel level) {
			List<VoidNodeEntry> nodes = VoidNodeDiscovery.listNodes(level, terminal.getNetworkKey(), terminal.getBlockPos());
			PacketDistributor.sendToPlayer(player, new VoidNodeListPacket(terminal.getBlockPos(), nodes));
		}
	}

	public static void sendPortableNodeList(ServerPlayer player, InteractionHand hand, NetworkKey networkKey) {
		if (!PortableVoidTerminalBinding.canInteract(player, networkKey))
			return;
		if (!PortableVoidTerminalBinding.isComplete(networkKey))
			return;
		List<VoidNodeEntry> nodes = VoidNodeDiscovery.listNodes(player.serverLevel(), networkKey, player.blockPosition());
		PacketDistributor.sendToPlayer(player, new PortableVoidTerminalListPacket(hand, networkKey, nodes));
	}

	public static boolean bindPortableTerminal(ServerPlayer player, InteractionHand hand,
			VoidNodeTerminalTileEntity terminal) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return false;
		if (!terminal.canOperate() || !terminal.getLink().canInteract(player))
			return false;

		NetworkKey networkKey = terminal.getNetworkKey();
		if (!PortableVoidTerminalBinding.isComplete(networkKey)) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.frequency_incomplete"),
					true);
			return false;
		}

		PortableVoidTerminalBinding.write(stack, networkKey, player.registryAccess());
		player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.bound"), true);
		return true;
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
		VoidNodeTerminalTileEntity terminal = resolveTerminal(player.serverLevel(), terminalPos);
		if (terminal == null)
			return false;
		if (!terminal.canOperate() || !terminal.getLink().canInteract(player))
			return false;
		if (terminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.teleport_cooldown"), true);
			return false;
		}

		ResourceLocation terminalDimension = WorldHelper.getDimensionID(terminal.getLevel());
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

		VoidNodeTerminalTileEntity targetTerminal = resolveTerminal(targetLevel, targetPos);
		if (targetTerminal != null && targetTerminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.target_cooldown"), true);
			return false;
		}

		int cost = VoidwayConfig.getVoidNodeTerminalTeleportFluidCostMb();
		if (terminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		terminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

		if (!teleportPlayerToTerminal(player, targetLevel, targetPos))
			return false;

		targetLevel.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	public static boolean teleportViaPortable(ServerPlayer player, InteractionHand hand, NetworkKey networkKey,
			ResourceLocation targetDimension, BlockPos targetPos) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(RWItems.PORTABLE_VOID_TERMINAL.get()))
			return false;

		Optional<NetworkKey> boundKey = PortableVoidTerminalBinding.read(stack, player.registryAccess())
				.filter(key -> key.equals(networkKey));
		if (boundKey.isEmpty()) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.unbound"), true);
			return false;
		}
		if (!PortableVoidTerminalBinding.canInteract(player, networkKey)) {
			player.displayClientMessage(Component.translatable("createvoidway.portable_void_terminal.frequency_denied"),
					true);
			return false;
		}

		if (!isSameNetwork(player.server, networkKey, targetDimension, targetPos))
			return false;

		ServerLevel targetLevel = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, targetDimension));
		if (targetLevel == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.dimension_unavailable"), true);
			return false;
		}

		VoidNodeTerminalTileEntity targetTerminal = resolveTerminal(targetLevel, targetPos);
		if (targetTerminal == null) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}
		if (targetTerminal.isTeleportOnCooldown()) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.target_cooldown"), true);
			return false;
		}

		int cost = VoidwayConfig.getPortableVoidTerminalTeleportFluidCostMb();
		if (targetTerminal.getFluidTank().getFluidAmount() < cost) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.insufficient_fluid"), true);
			return false;
		}
		targetTerminal.getFluidTank().drain(cost, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

		if (!teleportPlayerToTerminal(player, targetLevel, targetPos))
			return false;

		targetTerminal.startPortableTeleportCooldown();
		targetLevel.playSound(null, targetPos, net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT,
				net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1f);
		return true;
	}

	private static boolean teleportPlayerToTerminal(ServerPlayer player, ServerLevel targetLevel, BlockPos targetPos) {
		BlockState targetState = targetLevel.getBlockState(targetPos);
		if (!targetState.is(RWBlocks.VOID_NODE_TERMINAL.get())) {
			player.displayClientMessage(Component.translatable("createvoidway.void_node_terminal.invalid_target"), true);
			return false;
		}

		Vec3 target = VoidNodeTerminalLanding.findTeleportPos(targetLevel, targetPos, player);
		player.teleportTo(targetLevel, target.x, target.y, target.z, java.util.Collections.emptySet(),
				player.getYRot(), player.getXRot());
		VoidwaySableCompat.inheritSubLevelVelocity(targetLevel, player, target);
		return true;
	}

	@Nullable
	public static VoidNodeTerminalTileEntity resolveTerminal(ServerLevel level, BlockPos pos) {
		BlockPos basePos = VoidNodeTerminalMultiblock.getBasePos(level, pos);
		if (basePos == null)
			basePos = pos;
		if (level.getBlockEntity(basePos) instanceof VoidNodeTerminalTileEntity terminal)
			return terminal;
		return null;
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
