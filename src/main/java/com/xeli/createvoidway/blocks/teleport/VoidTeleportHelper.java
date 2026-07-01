package com.xeli.createvoidway.blocks.teleport;

import com.xeli.createvoidway.compat.VoidwaySableCompat;
import com.xeli.createvoidway.config.VoidwayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

public final class VoidTeleportHelper {

	public static final String CONTACT_COOLDOWN_KEY = "CreateVoidwayTeleportContactCooldown";

	private VoidTeleportHelper() {}

	public static boolean hasContactCooldown(Entity entity) {
		return entity.getPersistentData().getBoolean(CONTACT_COOLDOWN_KEY);
	}

	public static void setContactCooldown(Entity entity) {
		entity.getPersistentData().putBoolean(CONTACT_COOLDOWN_KEY, true);
	}

	public static void clearContactCooldown(Entity entity) {
		entity.getPersistentData().remove(CONTACT_COOLDOWN_KEY);
	}

	public static boolean canBeTeleported(Entity entity) {
		return entity.isAlive() && !entity.isPassenger() && !entity.isVehicle()
				&& (entity instanceof LivingEntity || entity instanceof ItemEntity || entity.canBeCollidedWith());
	}

	public static boolean isItemEntity(Entity entity) {
		return entity instanceof ItemEntity;
	}

	public static int getFluidCostFor(Entity entity) {
		if (entity instanceof ItemEntity)
			return VoidwayConfig.getVoidTeleportItemFluidCostMb();
		if (entity instanceof LivingEntity)
			return VoidwayConfig.getVoidTeleportLivingFluidCostMb();
		return VoidwayConfig.getVoidTeleportLivingFluidCostMb();
	}

	public static int computeBatchFluidCost(Iterable<Entity> entities) {
		int total = 0;
		for (Entity entity : entities)
			total += getFluidCostFor(entity);
		return total;
	}

	public static AABB getPlateFootprint(BlockPos padPos) {
		double margin = 1 / 16.0;
		double plateTop = padPos.getY() + VoidTeleportPadBlock.PLATE_HEIGHT;
		return new AABB(
				padPos.getX() + margin, plateTop,
				padPos.getZ() + margin,
				padPos.getX() + 1 - margin, plateTop,
				padPos.getZ() + 1 - margin);
	}

	/** Max distance above plate top (matches ~5/16 collision height). */
	public static final float CONTACT_FEET_TOLERANCE_ABOVE = 0.32f;

	public static AABB getContactSearchBounds(BlockPos padPos) {
		double margin = 1 / 16.0;
		double plateTop = padPos.getY() + VoidTeleportPadBlock.PLATE_HEIGHT;
		return new AABB(
				padPos.getX() + margin, plateTop - 0.08,
				padPos.getZ() + margin,
				padPos.getX() + 1 - margin, plateTop + CONTACT_FEET_TOLERANCE_ABOVE,
				padPos.getZ() + 1 - margin);
	}

	public static boolean isEntityInContactWithPad(BlockPos padPos, Entity entity) {
		AABB box = entity.getBoundingBox();
		double plateTop = padPos.getY() + VoidTeleportPadBlock.PLATE_HEIGHT;
		double feetY = box.minY;

		if (feetY < plateTop - 0.08 || feetY > plateTop + CONTACT_FEET_TOLERANCE_ABOVE)
			return false;
		if (!intersectsPlateHorizontally(box, padPos))
			return false;

		double midX = (box.minX + box.maxX) * 0.5;
		double midZ = (box.minZ + box.maxZ) * 0.5;
		BlockPos support = BlockPos.containing(midX, feetY - 0.02, midZ);
		return support.equals(padPos);
	}

	private static boolean intersectsPlateHorizontally(AABB entity, BlockPos padPos) {
		double margin = 1 / 16.0;
		double minX = padPos.getX() + margin;
		double maxX = padPos.getX() + 1 - margin;
		double minZ = padPos.getZ() + margin;
		double maxZ = padPos.getZ() + 1 - margin;
		return entity.maxX > minX && entity.minX < maxX
				&& entity.maxZ > minZ && entity.minZ < maxZ;
	}

	public static void teleportTo(ServerLevel level, Entity entity, BlockPos destinationPad) {
		teleportTo(level, entity, destinationPad, true);
	}

	public static void teleportTo(ServerLevel level, Entity entity, BlockPos destinationPad, boolean playEffects) {
		double yOffset = VoidTeleportPadBlock.PLATE_HEIGHT + 0.05;
		Vec3 target = VoidwaySableCompat.globalTeleportPos(level, destinationPad, yOffset);
		if (entity instanceof ServerPlayer player) {
			player.teleportTo(level, target.x, target.y, target.z, Collections.emptySet(),
					player.getYRot(), player.getXRot());
		} else if (entity instanceof LivingEntity living) {
			living.teleportTo(target.x, target.y, target.z);
		} else {
			entity.moveTo(target.x, target.y, target.z, entity.getYRot(), entity.getXRot());
			entity.setDeltaMovement(Vec3.ZERO);
		}
		entity.fallDistance = 0;
		VoidwaySableCompat.inheritSubLevelVelocity(level, entity, target);
		setContactCooldown(entity);
		if (playEffects)
			playTeleportEffects(level, destinationPad, BlockPos.containing(entity.position()));
	}

	public static void playBatchTeleportEffects(ServerLevel level, BlockPos sourcePad, BlockPos destinationPad) {
		playTeleportEffects(level, destinationPad, sourcePad);
	}

	public static void depositItemOnPad(ServerLevel level, BlockPos padPos, ItemStack stack) {
		if (stack.isEmpty())
			return;
		Vec3 target = VoidwaySableCompat.globalTeleportPos(level, padPos,
				VoidTeleportPadBlock.PLATE_HEIGHT + 0.05);
		ItemEntity entity = new ItemEntity(level, target.x, target.y, target.z, stack.copy());
		entity.setDefaultPickUpDelay();
		entity.setDeltaMovement(0, 0.15, 0);
		VoidwaySableCompat.inheritSubLevelVelocity(level, entity, target);
		level.addFreshEntity(entity);
	}

	public static void spawnItemAtPad(ServerLevel level, BlockPos destinationPad, ItemStack stack) {
		if (stack.isEmpty())
			return;
		Vec3 target = VoidwaySableCompat.globalTeleportPos(level, destinationPad,
				VoidTeleportPadBlock.PLATE_HEIGHT + 0.05);
		ItemEntity entity = new ItemEntity(level, target.x, target.y, target.z, stack.copy());
		entity.setDefaultPickUpDelay();
		entity.setDeltaMovement(0, 0.15, 0);
		VoidwaySableCompat.inheritSubLevelVelocity(level, entity, target);
		level.addFreshEntity(entity);
		setContactCooldown(entity);
		playTeleportEffects(level, destinationPad, BlockPos.containing(target));
	}

	private static void playTeleportEffects(ServerLevel level, BlockPos destinationPad, BlockPos sourcePos) {
		level.playSound(null, destinationPad, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 0.6f, 1.2f);
		level.playSound(null, sourcePos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1f);
	}

}
