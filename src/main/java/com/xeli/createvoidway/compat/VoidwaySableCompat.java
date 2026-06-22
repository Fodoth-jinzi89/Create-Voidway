package com.xeli.createvoidway.compat;

import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class VoidwaySableCompat {

	private VoidwaySableCompat() {
	}

	public static int distanceBlocks(@Nullable Level level, BlockPos from, BlockPos to) {
		double dx = (from.getX() + 0.5) - (to.getX() + 0.5);
		double dy = (from.getY() + 0.5) - (to.getY() + 0.5);
		double dz = (from.getZ() + 0.5) - (to.getZ() + 0.5);
		if (level == null)
			return Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz)));

		double distSq = SableCompanion.INSTANCE.distanceSquaredWithSubLevels(level,
				Vec3.atCenterOf(from), Vec3.atCenterOf(to));
		return Math.max(1, (int) Math.ceil(Math.sqrt(distSq)));
	}

	public static Vec3 globalTeleportPos(Level level, BlockPos pos, double yOffset) {
		Vec3 plot = Vec3.atBottomCenterOf(pos).add(0, yOffset, 0);
		return SableCompanion.INSTANCE.projectOutOfSubLevel(level, plot);
	}

	public static void inheritSubLevelVelocity(Level level, Entity entity, Vec3 globalPos) {
		Vec3 velocity = SableCompanion.INSTANCE.getVelocity(level, globalPos);
		if (velocity.lengthSqr() > 1.0E-6)
			entity.setDeltaMovement(entity.getDeltaMovement().add(velocity));
	}

	@Nullable
	public static Level levelFrom(LevelAccessor accessor) {
		return accessor instanceof Level level ? level : null;
	}

}
