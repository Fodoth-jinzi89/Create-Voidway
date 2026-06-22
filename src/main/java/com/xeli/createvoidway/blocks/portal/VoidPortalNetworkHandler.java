package com.xeli.createvoidway.blocks.portal;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.xeli.createvoidway.blocks.teleport.VoidTeleportLinkMetrics;
import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.blocks.voidtypes.VoidLinkBehaviour;
import com.xeli.createvoidway.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.createmod.catnip.levelWrappers.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class VoidPortalNetworkHandler {

	public enum PairStatus {
		UNPAIRED,
		VALID,
		CONFLICT
	}

	static final Map<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> connections = new HashMap<>();
	private static final Map<ResourceLocation, Long> lastCooldownSweep = new HashMap<>();

	public Set<BlockPos> getNetworkOf(LevelAccessor world, VoidPortalLinkBehaviour link) {
		Map<NetworkKey, Set<BlockPos>> networksInWorld = networksIn(world);
		NetworkKey key = link.getNetworkKey();
		return networksInWorld.computeIfAbsent(key, $ -> new LinkedHashSet<>());
	}

	public Map<NetworkKey, Set<BlockPos>> networksIn(LevelAccessor world) {
		return connections.computeIfAbsent(WorldHelper.getDimensionID(world), $ -> new HashMap<>());
	}

	public void onLoadWorld(LevelAccessor world) {
		networksIn(world);
		Create.LOGGER.debug("Prepared Void Portal Network Space for " + WorldHelper.getDimensionID(world));
	}

	public void onUnloadWorld(LevelAccessor world) {
		ResourceLocation id = WorldHelper.getDimensionID(world);
		connections.remove(id);
		lastCooldownSweep.remove(id);
		Create.LOGGER.debug("Removed Void Portal Network Space for " + id);
	}

	public void onPortalBlockChanged(LevelAccessor world, BlockPos pos) {
		VoidPortalShape shape = VoidPortalShape.findAt(world, pos);
		if (shape != null)
			refreshPortal(world, shape.connectorPos());
		invalidateNearbyPortals(world, pos);
	}

	private void invalidateNearbyPortals(LevelAccessor world, BlockPos changed) {
		for (Direction delta : Direction.values()) {
			BlockPos neighbor = changed.relative(delta);
			if (world.getBlockEntity(neighbor) instanceof VoidPortalConnectorTileEntity connector)
				connector.refreshShapeAndNetwork();
		}
	}

	public void refreshPortal(LevelAccessor world, BlockPos connectorPos) {
		if (world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector && !world.isClientSide())
			connector.updateCachedShape();

		VoidPortalLinkBehaviour link = getLinkBehaviour(world, connectorPos);
		if (link == null)
			return;

		VoidPortalShape shape = VoidPortalShape.findAt(world, connectorPos);
		Set<BlockPos> network = getNetworkOf(world, link);
		BlockPos previous = findRegisteredPos(network, connectorPos);

		if (shape != null && hasFrequencyConfigured(link))
			network.add(connectorPos);
		else
			network.remove(connectorPos);

		if (previous != null && !previous.equals(connectorPos))
			network.remove(previous);

		updateNetwork(world, link.getNetworkKey());

		if (world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity connector && !world.isClientSide()) {
			if (!network.contains(connectorPos))
				connector.setNetworkState(PairStatus.UNPAIRED, 0, null, 0);
			connector.refreshPortalBlocks();
		}
	}

	@Nullable
	private static BlockPos findRegisteredPos(Set<BlockPos> network, BlockPos connectorPos) {
		for (BlockPos pos : network) {
			if (pos.equals(connectorPos))
				return pos;
		}
		return null;
	}

	public void detachPortalFromNetwork(LevelAccessor world, BlockPos connectorPos) {
		VoidPortalLinkBehaviour link = getLinkBehaviour(world, connectorPos);
		if (link == null)
			return;
		Set<BlockPos> network = networksIn(world).get(link.getNetworkKey());
		if (network != null && network.remove(connectorPos))
			updateNetwork(world, link.getNetworkKey());
	}

	public void removePortalFromAllNetworks(LevelAccessor world, BlockPos connectorPos) {
		List<NetworkKey> affected = new ArrayList<>();
		for (var entry : networksIn(world).entrySet()) {
			if (entry.getValue().remove(connectorPos))
				affected.add(entry.getKey());
		}
		for (NetworkKey key : affected)
			updateNetwork(world, key);
		if (world.getBlockEntity(connectorPos) instanceof IVoidPortalEndpoint portal)
			portal.setNetworkState(PairStatus.UNPAIRED, 0, null, 0);
	}

	private void updateNetwork(LevelAccessor world, NetworkKey key) {
		Set<BlockPos> network = networksIn(world).get(key);
		if (network == null)
			return;

		for (Iterator<BlockPos> iterator = network.iterator(); iterator.hasNext(); ) {
			BlockPos pos = iterator.next();
			if (!isPortalAlive(world, pos) || !isValidPortal(world, pos))
				iterator.remove();
		}

		PairStatus status = getPairStatus(world, network);
		int count = network.size();
		int linkDistance = 0;
		if (status == PairStatus.VALID && count == 2) {
			Iterator<BlockPos> it = network.iterator();
			BlockPos first = it.next();
			BlockPos second = it.next();
			linkDistance = VoidTeleportLinkMetrics.computeDistanceBlocks(
					VoidwaySableCompat.levelFrom(world), first, second);
		}

		for (BlockPos pos : network) {
			BlockPos partner = status == PairStatus.VALID ? findPartner(pos, network) : null;
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof IVoidPortalEndpoint portal)
				portal.setNetworkState(status, count, partner, linkDistance);
		}

		for (BlockPos pos : network) {
			if (world.getBlockEntity(pos) instanceof VoidPortalConnectorTileEntity connector)
				connector.refreshPortalBlocks();
		}
	}

	private static boolean isValidPortal(LevelAccessor world, BlockPos connectorPos) {
		if (!(world.getBlockEntity(connectorPos) instanceof VoidPortalConnectorTileEntity))
			return false;
		if (VoidPortalShape.findAt(world, connectorPos) == null)
			return false;
		VoidPortalLinkBehaviour link = getLinkBehaviour(world, connectorPos);
		return link != null && hasFrequencyConfigured(link);
	}

	private static boolean isPortalAlive(LevelAccessor world, BlockPos pos) {
		if (!world.hasChunkAt(pos))
			return false;
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity instanceof VoidPortalConnectorTileEntity && !blockEntity.isRemoved();
	}

	private PairStatus getPairStatus(LevelAccessor world, Set<BlockPos> network) {
		int size = network.size();
		if (size < 2)
			return PairStatus.UNPAIRED;
		if (size > 2)
			return PairStatus.CONFLICT;
		return PairStatus.VALID;
	}

	public static boolean hasFrequencyConfigured(VoidPortalLinkBehaviour link) {
		return !link.getFrequencyStack(true).isEmpty() && !link.getFrequencyStack(false).isEmpty();
	}

	@Nullable
	private static VoidPortalLinkBehaviour getLinkBehaviour(LevelAccessor world, BlockPos pos) {
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		return behaviour instanceof VoidPortalLinkBehaviour portalLink ? portalLink : null;
	}

	private static BlockPos findPartner(BlockPos self, Set<BlockPos> network) {
		for (BlockPos pos : network) {
			if (!pos.equals(self))
				return pos;
		}
		return null;
	}

	public void tickCooldowns(ServerLevel level) {
		ResourceLocation dim = level.dimension().location();
		long time = level.getGameTime();
		if (lastCooldownSweep.getOrDefault(dim, -1L) == time)
			return;
		lastCooldownSweep.put(dim, time);

		for (Entity entity : level.getAllEntities()) {
			if (!VoidPortalHelper.hasContactCooldown(entity))
				continue;
			if (!touchesAnyPortal(level, entity))
				VoidPortalHelper.clearContactCooldown(entity);
		}
	}

	private static boolean touchesAnyPortal(LevelAccessor world, Entity entity) {
		Map<NetworkKey, Set<BlockPos>> networks = connections.get(WorldHelper.getDimensionID(world));
		if (networks == null)
			return false;
		for (Set<BlockPos> portals : networks.values()) {
			for (BlockPos pos : portals) {
				if (!world.hasChunkAt(pos))
					continue;
				VoidPortalShape shape = VoidPortalShape.findAt(world, pos);
				if (shape != null && world instanceof net.minecraft.world.level.Level level
						&& VoidPortalHelper.isEntityTouchingPortalBlock(level, shape, entity))
					return true;
			}
		}
		return false;
	}

	public void collectPositions(NetworkKey key, BiConsumer<ResourceLocation, BlockPos> consumer) {
		for (Map.Entry<ResourceLocation, Map<NetworkKey, Set<BlockPos>>> dimensionEntry : connections.entrySet()) {
			Set<BlockPos> positions = dimensionEntry.getValue().get(key);
			if (positions == null)
			 continue;
			ResourceLocation dimension = dimensionEntry.getKey();
			for (BlockPos pos : positions)
				consumer.accept(dimension, pos);
		}
	}

}
