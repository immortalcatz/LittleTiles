package com.creativemd.littletiles.common.structure;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

import com.creativemd.creativecore.common.utils.HashMapList;
import com.creativemd.creativecore.common.utils.WorldUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTile.LittleTilePosition;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.creativemd.littletiles.common.utils.small.LittleTileCoord;
import com.creativemd.littletiles.common.utils.small.LittleTileSize;
import com.creativemd.littletiles.common.utils.small.LittleTileVec;
import com.creativemd.littletiles.utils.PreviewTile;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructure {
	
	private static HashMap<String, LittleStructureEntry> structures = new HashMap<String, LittleStructureEntry>();
	
	public static ArrayList<String> getStructureNames()
	{
		ArrayList<String> result = new ArrayList<>();
		for (String id : structures.keySet()) {
			result.add(id);
		}
		return result;
	}
	
	public static void registerLittleStructure(String id, LittleStructure structure)
	{
		registerLittleStructure(id, new LittleStructureEntry(structure.getClass(), structure));
	}
	
	public static void registerLittleStructure(String id, LittleStructureEntry entry)
	{
		if(structures.containsKey(id))
			System.out.println("ID is already taken! id=" + id);
		else if(structures.containsValue(entry))
			System.out.println("Already registered class=" + entry);
		else{
			structures.put(id, entry);
		}
	}
	
	public static String getIDByClass(Class<? extends LittleStructure> classStructure)
	{
		for (String id : structures.keySet()) {
			if(classStructure.equals(structures.get(id).structureClass))
				return id;
		}
		return "";
	}
	
	public static Class<? extends LittleStructure> getClassByID(String id)
	{
		LittleStructureEntry entry = structures.get(id);
		if(entry != null)
			return entry.structureClass;
		return null;
	}
	
	public static LittleStructureEntry getEntryByID(String id)
	{
		return structures.get(id);
	}
	
	public static void initStructures()
	{
		registerLittleStructure("fixed", new LittleFixedStructure());
		registerLittleStructure("chair", new LittleChair());
		registerLittleStructure("door", new LittleDoor());
		registerLittleStructure("ladder", new LittleLadder());
		registerLittleStructure("bed", new LittleBed());
		registerLittleStructure("storage", new LittleStorage());
	}
	
	public static LittleStructure createAndLoadStructure(NBTTagCompound nbt, LittleTile mainTile)
	{
		if(nbt == null)
			return null;
		String id = nbt.getString("id");
		LittleStructureEntry entry = getEntryByID(id);
		if(entry != null)
		{
			Class<? extends LittleStructure> classStructure = entry.structureClass;
			if(classStructure != null)
			{
				LittleStructure structure = null;
				try {
					structure = classStructure.getConstructor().newInstance();
				} catch (Exception e) {
					System.out.println("Found invalid structureID=" + id);
				}
				structure.mainTile = mainTile;
				structure.loadFromNBT(nbt);
				return structure;
			}
		}
		return null;
	}
	
	public LittleTile mainTile;
	private ArrayList<LittleTile> tiles = null;
	
	public void setTiles(ArrayList<LittleTile> tiles)
	{
		this.tiles = tiles;
	}
	
	public ArrayList<LittleTile> getTiles()
	{
		if(tiles == null)
			if(!loadTiles())
			{
				ArrayList<LittleTile> tiles =  new ArrayList<>();
				return tiles;
			}
		return tiles;
	}
	
	public boolean hasLoaded()
	{
		loadTiles();
		return tilesToLoad == null || tilesToLoad.size() == 0;
	}
	
	public boolean loadTiles()
	{
		if(mainTile != null)
		{
			//System.out.println("loading Structure");
			if(tiles == null)
			{
				tiles = new ArrayList<LittleTile>();
				tiles.add(mainTile);
			}
			
			if(tilesToLoad == null)
				return true;
				
			int i = 0;
			while (i < tilesToLoad.size()) {
				if(checkForTile(mainTile.te.getWorld(), tilesToLoad.get(i)))
					tilesToLoad.remove(i);
				else
					i++;
			}
			
			if(tilesToLoad.size() == 0)
				tilesToLoad = null;
			return true;
		}
		return false;
	}
	
	//public ArrayList<LittleTilePosition> tilesToLoad = null;
	public ArrayList<LittleTileCoord> tilesToLoad = null;
	
	public ItemStack dropStack;
	
	public LittleStructure()
	{
		
	}
	
	public void loadFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("stack"))
		{
			dropStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
			if(dropStack == null)
			{
				nbt.getCompoundTag("stack").setString("id", LittleTiles.multiTiles.getRegistryName().toString());
				dropStack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
			}
		}
		
		
		//LoadTiles
		if(nbt.hasKey("count"))
		{
			tilesToLoad = new ArrayList<>();
			int count = nbt.getInteger("count");
			for (int i = 0; i < count; i++) {
				LittleTileCoord coord = null;
				if(nbt.hasKey("i" + i + "coX"))
				{
					LittleTilePosition pos = new LittleTilePosition("i" + i, nbt);
					coord = new LittleTileCoord(mainTile.te, pos.coord, pos.position);
				}else{
					coord = new LittleTileCoord("i" + i, nbt);
				}
				
				tilesToLoad.add(coord);
			}
		}
		
		loadFromNBTExtra(nbt);
	}
	
	protected abstract void loadFromNBTExtra(NBTTagCompound nbt);
	
	public void writeToNBT(NBTTagCompound nbt)
	{
		if(dropStack != null)
		{
			NBTTagCompound nbtStack = new NBTTagCompound();
			dropStack.writeToNBT(nbtStack);
			nbt.setTag("stack", nbtStack);
		}
		
		nbt.setString("id", getIDOfStructure());
		
		//SaveTiles
		if(tiles != null)
		{
			nbt.setInteger("count", tiles.size());
			for (int i = 0; i < tiles.size(); i++) {
				if(tiles.get(i).isStructureBlock)
				{
					tiles.get(i).updateCorner();
					new LittleTileCoord(mainTile.te, tiles.get(i).te.getPos(), tiles.get(i).cornerVec.copy()).writeToNBT("i" + i, nbt);;
					//new LittleTilePosition().writeToNBT("i" + i, nbt);
				}
				//tiles.get(i).pos.writeToNBT("i" + i, nbt);
			}
		}
		
		if(tilesToLoad != null)
		{
			int start = nbt.getInteger("count");
			nbt.setInteger("count", start + tilesToLoad.size());
			for (int i = 0; i < tilesToLoad.size(); i++) {
				tilesToLoad.get(i).writeToNBT("i" + (i + start), nbt);
			}
		}
		
		//if(mainTile == null)
			//System.out.println("Couldn't save tiles!!!" + mainTile.te.getCoord());
		
		writeToNBTExtra(nbt);
	}
	
	protected abstract void writeToNBTExtra(NBTTagCompound nbt);
	
	public boolean checkForTile(World world, LittleTileCoord pos)
	{
		BlockPos coord = pos.getAbsolutePosition(mainTile.te);
		Chunk chunk = world.getChunkFromBlockCoords(coord);
		if(WorldUtils.checkIfChunkExists(chunk))
		{
			//chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(coord);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
				if(tile != null && tile.isStructureBlock)
				{
					if(!tiles.contains(tile))
						tiles.add(tile);
					tile.structure = this;
					return true;
				}
			}
		}
		return false;
	}
	
	/*@Deprecated
	public boolean checkForTile(World world, LittleTilePosition pos)
	{
		Chunk chunk = world.getChunkFromBlockCoords(pos.coord.posX, pos.coord.posZ);
		if(!(chunk instanceof EmptyChunk))
		{
			//chunk.isChunkLoaded
			TileEntity tileEntity = world.getTileEntity(pos.coord.posX, pos.coord.posY, pos.coord.posZ);
			if(tileEntity instanceof TileEntityLittleTiles)
			{
				LittleTile tile = ((TileEntityLittleTiles) tileEntity).getTile(pos.position);
				if(tile != null && tile.isStructureBlock)
				{
					if(!tiles.contains(tile))
						tiles.add(tile);
					tile.structure = this;
					return true;
				}
			}
		}
		return false;
	}*/
	
	//====================Rendering====================
	
	public ArrayList<PreviewTile> getSpecialTiles()
	{
		return new ArrayList<>();
	}
	
	//====================LittleTile-Stuff====================
	
	public void onLittleTileDestory()
	{
		ArrayList<LittleTile> tiles = getTiles();
		for (int i = 0; i < tiles.size(); i++) {
			tiles.get(i).te.removeTile(tiles.get(i));
			//tiles.get(i).destroy();
		}
	}
	
	public ItemStack getStructureDrop()
	{
		return dropStack.copy();
	}
	
	public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
	
	//====================SORTING====================
	
	public HashMapList<BlockPos, LittleTile> getTilesSortedPerBlock()
	{
		HashMapList<BlockPos, LittleTile> coords = new HashMapList<>();
		ArrayList<LittleTile> tiles = getTiles();
		for (int i = 0; i < tiles.size(); i++) {
			coords.add(tiles.get(i).te.getPos(), tiles.get(i));
		}
		return coords;
	}
	
	public void onFlip(World world, EntityPlayer player, ItemStack stack, EnumFacing direction){}
	
	public void onRotate(World world, EntityPlayer player, ItemStack stack, EnumFacing direction){}
	
	//====================GUI STUFF====================
	@SideOnly(Side.CLIENT)
	public abstract void createControls(SubGui gui, LittleStructure structure);
	
	@SideOnly(Side.CLIENT)
	public abstract LittleStructure parseStructure(SubGui gui);
	
	
	//====================Helpers====================
	public LittleTileSize getSize()
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		HashMapList<BlockPos, LittleTile> coords = getTilesSortedPerBlock();
		if(coords.sizeOfValues() == 0)
			return null;
		for (BlockPos coord : coords.getKeys()) {
			ArrayList<LittleTile> values = coords.getValues(coord);
			for (int j = 0; j < values.size(); j++) {
				for (int h = 0; h < values.get(j).boundingBoxes.size(); h++) {
					LittleTileBox box = values.get(j).boundingBoxes.get(h);
					minX = Math.min(minX, coord.getX()*16+box.minX);
					minY = Math.min(minY, coord.getY()*16+box.minY);
					minZ = Math.min(minZ, coord.getZ()*16+box.minZ);
					
					maxX = Math.max(maxX, coord.getX()*16+box.maxX);
					maxY = Math.max(maxY, coord.getY()*16+box.maxY);
					maxZ = Math.max(maxZ, coord.getZ()*16+box.maxZ);
				}
			}
			/*
			minX = Math.min(minX, coord.posX);
			minY = Math.min(minY, coord.posY);
			minZ = Math.min(minZ, coord.posZ);
			
			maxX = Math.max(maxX, coord.posX);
			maxY = Math.max(maxY, coord.posY);
			maxZ = Math.max(maxZ, coord.posZ);*/
		}
		
		return new LittleTileBox(minX, minY, minZ, maxX, maxY, maxZ).getSize();
	}
	
	public LittleTileVec getHighestCenterPoint()
	{
		int minYPos = Integer.MAX_VALUE;
		
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int minZ = Integer.MAX_VALUE;
		
		int maxYPos = Integer.MIN_VALUE;
		
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int maxZ = Integer.MIN_VALUE;
		
		HashMapList<BlockPos, LittleTile> coords = getTilesSortedPerBlock();
		if(coords.sizeOfValues() == 0)
			return null;
		for (BlockPos coord : coords.getKeys()) {
			ArrayList<LittleTile> values = coords.getValues(coord);
			for (int j = 0; j < values.size(); j++) {
				for (int h = 0; h < values.get(j).boundingBoxes.size(); h++) {
					LittleTileBox box = values.get(j).boundingBoxes.get(h);
					minX = Math.min(minX, coord.getX()*16+box.minX);
					minY = Math.min(minY, coord.getY()*16+box.minY);
					minZ = Math.min(minZ, coord.getZ()*16+box.minZ);
					
					maxX = Math.max(maxX, coord.getX()*16+box.maxX);
					maxY = Math.max(maxY, coord.getY()*16+box.maxY);
					maxZ = Math.max(maxZ, coord.getZ()*16+box.maxZ);
				}
				minYPos = Math.min(minYPos, coord.getY());
				maxYPos = Math.max(maxYPos, coord.getY());
			}
			/*
			minX = Math.min(minX, coord.posX);
			minY = Math.min(minY, coord.posY);
			minZ = Math.min(minZ, coord.posZ);
			
			maxX = Math.max(maxX, coord.posX);
			maxY = Math.max(maxY, coord.posY);
			maxZ = Math.max(maxZ, coord.posZ);*/
		}
		
		//double test = Math.floor(((minX+maxX)/16D/2D));
		int centerX = (int) Math.floor((minX+maxX)/16D/2D);
		int centerY = (int) Math.floor((minY+maxY)/16D/2D);
		int centerZ = (int) Math.floor((minZ+maxZ)/16D/2D);
		
		int centerTileX = (int) (Math.floor(minX+maxX)/2D)-centerX*16;
		int centerTileY = (int) (Math.floor(minY+maxY)/2D)-centerY*16;
		int centerTileZ = (int) (Math.floor(minZ+maxZ)/2D)-centerZ*16;
		
		LittleTileVec position = new LittleTileVec((minX+maxX)/2, minYPos*16, (minZ+maxZ)/2);
		//position.y = ;
		for (int y = minYPos; y <= maxYPos; y++) {
			ArrayList<LittleTile> tilesInCenter = coords.getValues(new BlockPos(centerX, y, centerZ));
			if(tilesInCenter != null)
			{
				LittleTileBox box = new LittleTileBox(centerTileX, LittleTile.minPos, centerTileZ, centerTileX+1, LittleTile.maxPos, centerTileZ+1);
				//int highest = LittleTile.minPos;
				for (int i = 0; i < tilesInCenter.size(); i++) {
					for (int j = 0; j < tilesInCenter.get(i).boundingBoxes.size(); j++) {
						LittleTileBox littleBox = tilesInCenter.get(i).boundingBoxes.get(j);
						if(box.intersectsWith(littleBox))
						{
							position.y = Math.max(y*16+littleBox.maxY, position.y);
							//highest = Math.max(highest, littleBox.maxY);
						}
					}
				}
				
			}
		}
		
		
		return position;
	}
	
	//====================LittleStructure ID====================
	
	public String getIDOfStructure()
	{
		return getIDByClass(this.getClass());
	}
	
	public static class LittleStructureEntry {
		
		public Class<? extends LittleStructure> structureClass;
		public LittleStructure parser;
		
		public LittleStructureEntry(Class<? extends LittleStructure> structureClass, LittleStructure parser)
		{
			this.structureClass = structureClass;
			this.parser = parser;
		}
		
		@Override
		public boolean equals(Object object)
		{
			return object instanceof LittleStructureEntry && ((LittleStructureEntry) object).structureClass == this.structureClass;
		}
		
		@Override
		public String toString()
		{
			return structureClass.toString();
		}
	}
	
	public boolean isBed(IBlockAccess world, BlockPos pos, EntityLivingBase player)
	{
		return false;
	}
	
	public EnumFacing getBedDirection(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return EnumFacing.EAST;
    }

	public boolean isLadder()
	{
		return false;
	}
	
}
