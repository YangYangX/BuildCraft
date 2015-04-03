package buildcraft.robotics.ai;

import java.util.Iterator;
import java.util.LinkedList;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IZone;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.core.lib.utils.BlockScannerExpanding;
import buildcraft.core.lib.utils.BlockScannerRandom;
import buildcraft.core.lib.utils.BlockScannerZoneRandom;
import buildcraft.core.lib.utils.IBlockFilter;
import buildcraft.core.lib.utils.IterableAlgorithmRunner;
import buildcraft.core.lib.utils.PathFindingSearch;

public class AIRobotSearchBlock extends AIRobot {

	public BlockIndex blockFound;
	public LinkedList<BlockIndex> path;
	private PathFindingSearch blockScanner = null;
	private IterableAlgorithmRunner blockScannerJob;
	private IBlockFilter pathFound;
	private Iterator<BlockIndex> blockIter;

	public AIRobotSearchBlock(EntityRobotBase iRobot, boolean random, IBlockFilter iPathFound) {
		super(iRobot);

		pathFound = iPathFound;
		if (!random) {
			blockIter = new BlockScannerExpanding().iterator();
		} else {
			IZone zone = iRobot.getZoneToWork();
			if (zone != null) {
				BlockIndex pos = new BlockIndex(iRobot);
				blockIter = new BlockScannerZoneRandom(pos.x, pos.y, pos.z, iRobot.worldObj.rand, zone)
						.iterator();
			} else {
				blockIter = new BlockScannerRandom(iRobot.worldObj.rand, 64).iterator();
			}
		}
		blockFound = null;
		path = null;
	}

	@Override
	public void start() {
		blockScanner = new PathFindingSearch(robot.worldObj, new BlockIndex(
				robot), blockIter, pathFound, 96, robot
				.getZoneToWork());
		blockScannerJob = new IterableAlgorithmRunner(blockScanner);
		blockScannerJob.start();
	}

	@Override
	public void update() {
		if (blockScannerJob == null) {
			// This is probably due to a load from NBT. Abort the ai in
			// that case, since there's no filter to analyze either.
			abort();
			return;
		}

		if (blockScannerJob.isDone()) {
			path = blockScanner.getResult();

			if (path != null && path.size() > 0) {
				blockFound = path.removeLast();
			} else {
				path = null;
			}

			terminate();
		}
	}

	@Override
	public void end() {
		if (blockScannerJob != null) {
			blockScannerJob.terminate();
		}
	}

	@Override
	public boolean success() {
		return blockFound != null;
	}

	@Override
	public void writeSelfToNBT(NBTTagCompound nbt) {
		super.writeSelfToNBT(nbt);

		if (blockFound != null) {
			NBTTagCompound sub = new NBTTagCompound();
			blockFound.writeTo(sub);
			nbt.setTag("blockFound", sub);
		}
	}

	@Override
	public void loadSelfFromNBT(NBTTagCompound nbt) {
		super.loadSelfFromNBT(nbt);

		if (nbt.hasKey("blockFound")) {
			blockFound = new BlockIndex(nbt.getCompoundTag("blockFound"));
		}
	}

	public boolean takeResource() {
		boolean taken = false;
		if (robot.getRegistry().take(new ResourceIdBlock(blockFound), robot)) {
			taken = true;
		}
		unreserve();
		return taken;
	}

	public void unreserve() {
		blockScanner.unreserve(blockFound);
	}

	@Override
	public int getEnergyCost() {
		return 2;
	}

}