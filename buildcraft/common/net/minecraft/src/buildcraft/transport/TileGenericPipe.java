/** 
 * BuildCraft is open-source. It is distributed under the terms of the 
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.Packet;
import net.minecraft.src.Packet230ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraftCore;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.ILiquidContainer;
import net.minecraft.src.buildcraft.api.IPipeEntry;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.ISpecialInventory;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.api.TileNetworkData;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.ISynchronizedTile;
import net.minecraft.src.buildcraft.core.PacketIds;

public class TileGenericPipe extends TileEntity implements IPowerReceptor,
		ILiquidContainer, ISpecialInventory, IPipeEntry, ISynchronizedTile {
	
	public Pipe pipe;
	private boolean blockNeighborChange = false;
	private boolean initialized = false;

	@TileNetworkData public int pipeId = -1;
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		if (pipe != null) {		
			nbttagcompound.setInteger("pipeId", pipe.itemID);
			pipe.writeToNBT(nbttagcompound);
		}
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		
		pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, nbttagcompound.getInteger("pipeId"));
		pipe.setTile(this);
		pipe.readFromNBT(nbttagcompound);	
	}
		
	@Override
	public void validate () {
		super.validate();
		
		if (pipe == null) {
			pipe = BlockGenericPipe.pipeBuffer.get(new BlockIndex(xCoord, yCoord, zCoord));			
		}
		
		if (BlockGenericPipe.pipeBuffer.containsKey(new BlockIndex(xCoord, yCoord, zCoord))) {
			BlockGenericPipe.pipeBuffer.remove(new BlockIndex(xCoord, yCoord, zCoord));	
		}
		
		if (pipe != null) {
			pipe.setTile(this);
			pipe.setWorld(worldObj);
			pipeId = pipe.itemID;
		}
	}
	
	@Override
	public void updateEntity () {
		if (!BlockGenericPipe.isValid(pipe)) {
			return;
		}
		
		pipeId = pipe.itemID;
		
		if (!initialized) {		
			pipe.initialize();
			pipe.setWorld(worldObj);
			pipe.setTile(this);
			initialized = true;
		}
		
		if (blockNeighborChange) {
			pipe.onNeighborBlockChange();
			blockNeighborChange = false;
		}
		
		PowerProvider provider = getPowerProvider();
		
		if (provider != null) {			
			provider.update(this);
		}
		
		pipe.updateEntity ();
	}

	@Override
	public void setPowerProvider(PowerProvider provider) {		
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).setPowerProvider(provider);
		}
		
	}

	@Override
	public PowerProvider getPowerProvider() {
		if (pipe instanceof IPowerReceptor) {
			return ((IPowerReceptor) pipe).getPowerProvider();
		} else {
			return null;
		}
	}

	@Override
	public void doWork() {
		if (pipe instanceof IPowerReceptor) {
			((IPowerReceptor) pipe).doWork();
		}		
	}

	@Override
	public int fill(Orientations from, int quantity, int id, boolean doFill) {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).fill(from, quantity, id, doFill);
		} else {
			return 0;	
		}		
	}

	@Override
	public int empty(int quantityMax, boolean doEmpty) {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).empty(quantityMax, doEmpty);
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidQuantity() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidQuantity();
		} else {
			return 0;	
		}		
	}

	@Override
	public int getCapacity() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getCapacity();
		} else {
			return 0;
		}
	}

	@Override
	public int getLiquidId() {
		if (pipe.transport instanceof ILiquidContainer) {
			return ((ILiquidContainer) pipe.transport).getLiquidId();
		} else {
			return 0;
		}
	}
	
	public void scheduleNeighborChange() {
		blockNeighborChange  = true;
	}

	@Override
	public int getSizeInventory() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.getSizeInventory();
		} else {
			return 0;
		}
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.getStackInSlot(i);
		} else {
			return null;
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.decrStackSize(i, j);
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.logic.setInventorySlotContents(i, itemstack);
		}
	}

	@Override
	public String getInvName() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.getInvName();
		} else {
			return "";
		}
	}

	@Override
	public int getInventoryStackLimit() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.getInventoryStackLimit();
		} else {
			return 0;
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.canInteractWith(entityplayer);
		} else {
			return false;
		}
	}

	@Override
	public boolean addItem(ItemStack stack, boolean doAdd, Orientations from) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.addItem(stack, doAdd, from);
		} else {
			return false;
		}
	}

	@Override
	public ItemStack extractItem(boolean doRemove, Orientations from) {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.logic.extractItem(doRemove, from);
		} else {
			return null;
		}
	}

	@Override
	public void entityEntering(EntityPassiveItem item, Orientations orientation) {
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.transport.entityEntering (item, orientation);
		}		
	}

	@Override
	public boolean acceptItems() {
		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.transport.acceptItems();
		} else {
			return false;
		}
	}

	@Override
	public void handleDescriptionPacket(Packet230ModLoader packet) {
		if (pipe == null) {
			pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, packet.dataInt [3]);
			pipe.setTile(this);	
			pipe.setWorld(worldObj);
		}
	}

	@Override
	public void handleUpdatePacket(Packet230ModLoader packet) {
		if (!BlockGenericPipe.isValid(pipe) && pipeId != -1) {
			pipe = BlockGenericPipe.createPipe(xCoord, yCoord, zCoord, pipeId);
			pipe.setTile(this);	
			pipe.setWorld(worldObj);
		}
		
		if (BlockGenericPipe.isValid(pipe)) {
			pipe.handlePacket(packet);
		}
	}

	@Override
	public void postPacketHandling(Packet230ModLoader packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Packet230ModLoader getUpdatePacket() {
		return pipe.getNetworkPacket();
	}

	@Override
	public Packet getDescriptionPacket() {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = mod_BuildCraftCore.instance.getId();
		packet.isChunkDataPacket = true;
		packet.packetType = PacketIds.TileDescription.ordinal();
		
		packet.dataInt = new int [4];
		packet.dataInt [0] = xCoord;
		packet.dataInt [1] = yCoord;
		packet.dataInt [2] = zCoord;
		packet.dataInt [3] = pipe.itemID;
		
		return packet;
	}

	@Override
	public void openChest() {
		
	}

	@Override
	public void closeChest() {
		
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

}
