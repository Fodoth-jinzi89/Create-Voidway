package com.xeli.createvoidway.blocks.voidtypes.battery;

import net.neoforged.neoforge.energy.IEnergyStorage;

public class VoidBatteryFilteredEnergyStorage implements IEnergyStorage {

	public enum Mode {
		INSERT_ONLY,
		EXTRACT_ONLY,
		BLOCKED
	}

	private final IEnergyStorage delegate;
	private final Mode mode;
	private final float transferLossFraction;

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode) {
		this(delegate, mode, 0f);
	}

	public VoidBatteryFilteredEnergyStorage(IEnergyStorage delegate, Mode mode, float transferLossFraction) {
		this.delegate = delegate;
		this.mode = mode;
		this.transferLossFraction = Math.clamp(transferLossFraction, 0f, 1f);
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if (mode != Mode.INSERT_ONLY)
			return 0;
		int stored = delegate.receiveEnergy(applyInsertLoss(maxReceive), simulate);
		return Math.min(reverseInsertLoss(stored), maxReceive);
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if (mode != Mode.EXTRACT_ONLY)
			return 0;
		if (transferLossFraction <= 0f)
			return delegate.extractEnergy(maxExtract, simulate);
		int requestedFromChannel = reverseExtractLoss(maxExtract);
		int extracted = delegate.extractEnergy(requestedFromChannel, simulate);
		return Math.min(applyExtractLoss(extracted), maxExtract);
	}

	private int applyInsertLoss(int amount) {
		if (transferLossFraction <= 0f || amount <= 0)
			return amount;
		return (int) (amount * (1f - transferLossFraction));
	}

	private int reverseInsertLoss(int stored) {
		if (transferLossFraction <= 0f || stored <= 0)
			return stored;
		float efficiency = 1f - transferLossFraction;
		if (efficiency <= 0f)
			return 0;
		return Math.min((int) Math.ceil(stored / efficiency), Integer.MAX_VALUE);
	}

	private int applyExtractLoss(int amount) {
		if (transferLossFraction <= 0f || amount <= 0)
			return amount;
		return (int) (amount * (1f - transferLossFraction));
	}

	private int reverseExtractLoss(int desiredDelivery) {
		if (transferLossFraction <= 0f || desiredDelivery <= 0)
			return desiredDelivery;
		float efficiency = 1f - transferLossFraction;
		if (efficiency <= 0f)
			return 0;
		return Math.min((int) Math.ceil(desiredDelivery / efficiency), Integer.MAX_VALUE);
	}

	@Override
	public int getEnergyStored() {
		return delegate.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored() {
		return delegate.getMaxEnergyStored();
	}

	@Override
	public boolean canExtract() {
		return mode == Mode.EXTRACT_ONLY && delegate.canExtract();
	}

	@Override
	public boolean canReceive() {
		return mode == Mode.INSERT_ONLY && delegate.canReceive();
	}

}
