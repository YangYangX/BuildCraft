/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import buildcraft.api.power.ILaserTarget;
import buildcraft.core.TileBuildCraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class TileLaserTableBase extends TileBuildCraft implements ILaserTarget {

	public double clientRequiredEnergy = 0;
	private double[] recentEnergy = new double[20];
	private double energy = 0;
	private int tick = 0;
	private int recentEnergyAverage;

	@Override
	public void updateEntity() { // WARNING: run only server-side, see canUpdate()
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void addEnergy(double energy) {
		this.energy += energy;
	}

	public void subtractEnergy(double energy) {
		this.energy -= energy;
	}

	public abstract double getRequiredEnergy();

	public double getCompletionRatio(float ratio) {
		if (!canCraft())
			return 0;
		if (energy >= clientRequiredEnergy)
			return ratio;
		return energy / clientRequiredEnergy * ratio;
	}

	public int getRecentEnergyAverage() {
		return recentEnergyAverage;
	}

	public abstract boolean canCraft();

	@Override
	public boolean requiresLaserEnergy() {
		return canCraft() && energy < getRequiredEnergy() * 5F;
	}

	@Override
	public void receiveLaserEnergy(float energy) {
		energy += energy;
		recentEnergy[tick] += energy;
	}

	@Override
	public boolean isInvalidTarget() {
		return isInvalid();
	}

	@Override
	public int getXCoord() {
		return xCoord;
	}

	@Override
	public int getYCoord() {
		return yCoord;
	}

	@Override
	public int getZCoord() {
		return zCoord;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		energy = nbt.getDouble("energy");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("energy", energy);
	}
	/* SMP GUI */

	public void getGUINetworkData(int i, int j) {
		int currentStored = (int) (energy * 100.0);
		int requiredEnergy = (int) (clientRequiredEnergy * 100.0);
		switch (i) {
			case 0:
				requiredEnergy = (requiredEnergy & 0xFFFF0000) | (j & 0xFFFF);
				clientRequiredEnergy = (requiredEnergy / 100.0f);
				break;
			case 1:
				currentStored = (currentStored & 0xFFFF0000) | (j & 0xFFFF);
				energy = (currentStored / 100.0f);
				break;
			case 2:
				requiredEnergy = (requiredEnergy & 0xFFFF) | ((j & 0xFFFF) << 16);
				clientRequiredEnergy = (requiredEnergy / 100.0f);
				break;
			case 3:
				currentStored = (currentStored & 0xFFFF) | ((j & 0xFFFF) << 16);
				energy = (currentStored / 100.0f);
				break;
			case 4:
				recentEnergyAverage = recentEnergyAverage & 0xFFFF0000 | (j & 0xFFFF);
				break;
			case 5:
				recentEnergyAverage = (recentEnergyAverage & 0xFFFF) | ((j & 0xFFFF) << 16);
				break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		int requiredEnergy = (int) (getRequiredEnergy() * 100.0);
		int currentStored = (int) (energy * 100.0);
		int lRecentEnergy = 0;
		for (int i = 0; i < recentEnergy.length; i++) {
			lRecentEnergy += (int) (recentEnergy[i] * 100.0 / (recentEnergy.length - 1));
		}
		iCrafting.sendProgressBarUpdate(container, 0, requiredEnergy & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 1, currentStored & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 2, (requiredEnergy >>> 16) & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 3, (currentStored >>> 16) & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 4, lRecentEnergy & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 5, (lRecentEnergy >>> 16) & 0xFFFF);
	}
}
