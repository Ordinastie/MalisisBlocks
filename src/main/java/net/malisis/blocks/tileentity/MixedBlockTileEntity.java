package net.malisis.blocks.tileentity;

import net.malisis.blocks.item.MixedBlockBlockItem;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

public class MixedBlockTileEntity extends TileEntity
{
	private IBlockState state1;
	private IBlockState state2;

	public void set(ItemStack itemStack)
	{
		Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(itemStack.getTagCompound());
		state1 = pair.getLeft();
		state2 = pair.getRight();
	}

	public IBlockState getState1()
	{
		return state1;
	}

	public IBlockState getState2()
	{
		return state2;
	}

	public boolean isOpaque()
	{
		if (state1 == null)
			return true;
		return !(state1.getBlock() instanceof BlockBreakable || state2.getBlock() instanceof BlockBreakable);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(nbt);
		state1 = pair.getLeft();
		state2 = pair.getRight();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		MixedBlockBlockItem.writeNBT(nbt, state1, state2);
		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return oldState.getBlock() != newSate.getBlock();
	}
}
