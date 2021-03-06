package com.creativemd.littletiles.common.items;

import com.creativemd.littletiles.common.blocks.ISpecialBlockSelector;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemUtilityKnife extends Item implements ISpecialBlockSelector {
	
	public ItemUtilityKnife()
	{
		setCreativeTab(CreativeTabs.TOOLS);
		hasSubtypes = true;
		setMaxStackSize(1);
	}

	@Override
	public LittleTileBox getBox(TileEntityLittleTiles te, LittleTile tile, BlockPos pos, EntityPlayer player) {
		if(tile.isStructureBlock)
			return null;
		RayTraceResult result = te.getMoving(player);
		LittleTileVec vec = new LittleTileVec(result.hitVec);
		vec.subVec(new LittleTileVec(pos.getX()*16, pos.getY()*16, pos.getZ()*16));;
		switch(result.sideHit)
		{
		case EAST:
			vec.x--;
			break;
		case UP:
			vec.y--;
		case DOWN:
			vec.x--;
			break;
		case SOUTH:
			vec.z--;
		case NORTH:
			vec.x--;
			break;
		default:
			break;
		}
		return new LittleTileBox(vec);
		//return new AxisAlignedBB(result.hitVec.xCoord, result.hitVec.yCoord, result.hitVec.zCoord, result.hitVec.xCoord+1/16D, result.hitVec.yCoord+1/16D, result.hitVec.zCoord+1/16D);
	}
	
}
