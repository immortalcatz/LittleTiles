package com.creativemd.littletiles.utils;

import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ShiftHandler {
	
	@SideOnly(Side.CLIENT)
	public abstract void handleRendering(Minecraft mc, double x, double y, double z);
	
	public abstract double getDistance(LittleTileVec suggestedPos);
	
	protected abstract LittleTileBox getNewPos(World world, BlockPos pos, LittleTileBox suggested);
	
	public void init(World world, BlockPos pos)
	{
		
	}
	
	public LittleTileBox getNewPosition(World world, BlockPos pos, LittleTileBox suggested)
	{
		/*suggested.minX *= 16;
		suggested.minY *= 16;
		suggested.minZ *= 16;
		suggested.maxX *= 16;
		suggested.maxY *= 16;
		suggested.maxZ *= 16;
		*/
		LittleTileBox oldBox = suggested.copy();
		LittleTileBox newBox = getNewPos(world, pos, suggested);
		
		//TileEntity te = world.getTileEntity(x, y, z);
		//if(te instanceof TileEntityLittleTiles)
			//if(!((TileEntityLittleTiles)te).isSpaceForLittleTile(newBox.getBox()))
				//return oldBox;
		
		if(newBox != null)
			return newBox;
		return oldBox;
	}
	
}
