/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.blocks.vanishingoption;

import java.util.HashMap;
import java.util.Map;

import net.malisis.blocks.block.VanishingBlock;
import net.malisis.blocks.network.VanishingDiamondFrameMessage.DataType;
import net.malisis.blocks.tileentity.VanishingTileEntity;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class VanishingOptions implements IDirectInventoryProvider
{
	private MalisisInventory inventory;
	private ItemStack itemStack;
	private int duration = 8;
	protected Map<EnumFacing, DirectionState> directionStates = new HashMap<>();

	public VanishingOptions()
	{
		inventory = new MalisisInventory(this, 1);
		inventory.setInventoryStackLimit(1);
		for (EnumFacing dir : EnumFacing.VALUES)
			directionStates.put(dir, new DirectionState(dir));
	}

	public VanishingOptions(ItemStack itemStack)
	{
		this();
		this.itemStack = itemStack;
		inventory.setInventoryStackLimit(64);
	}

	@Override
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return null;
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public MalisisSlot getSlot()
	{
		return inventory.getSlot(0);
	}

	public void set(EnumFacing dir, DataType type, int time, boolean enabled)
	{
		DirectionState ds = getDirectionState(dir);

		switch (type)
		{
			case PROPAGATION:
				ds.shouldPropagate = enabled;
				break;
			case DELAY:
				ds.delay = time;
				break;
			case INVERSED:
				ds.inversed = enabled;
				break;
			case DURATION:
				setDuration(time);
				break;
		}
	}

	public DirectionState getDirectionState(EnumFacing dir)
	{
		return directionStates.get(dir);
	}

	public void setPowerState(World world, BlockPos pos, int timer, boolean powered)
	{
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			directionStates.get(dir).resetPropagationState();
			directionStates.get(dir).propagateState(world, pos, timer, powered);
		}
	}

	public void propagateState(World world, BlockPos pos, int timer, boolean powered)
	{
		for (EnumFacing dir : EnumFacing.VALUES)
			directionStates.get(dir).propagateState(world, pos, timer, powered);
	}

	public void copy(VanishingOptions options)
	{
		duration = options.duration;
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			getDirectionState(dir).copy(options.getDirectionState(dir));
		}
	}

	public void save()
	{
		if (itemStack == null)
			return;

		if (itemStack.getTagCompound() == null)
			itemStack.setTagCompound(new NBTTagCompound());

		writeToNBT(itemStack.getTagCompound());
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		inventory.readFromNBT(nbt);
		duration = nbt.hasKey("Duration") ? nbt.getInteger("Duration") : VanishingTileEntity.maxTransitionTime;

		NBTTagList dirList = nbt.getTagList("Directions", NBT.TAG_COMPOUND);
		for (int i = 0; i < dirList.tagCount(); ++i)
		{
			NBTTagCompound tag = dirList.getCompoundTagAt(i);
			EnumFacing dir = EnumFacing.getFront(tag.getInteger("direction"));
			directionStates.get(dir).readFromNBT(tag);
		}
	}

	public void writeToNBT(NBTTagCompound nbt)
	{
		inventory.writeToNBT(nbt);
		nbt.setInteger("Duration", getDuration());
		NBTTagList dirList = new NBTTagList();
		for (EnumFacing dir : EnumFacing.VALUES)
			dirList.appendTag(directionStates.get(dir).writeToNBT(new NBTTagCompound()));
		nbt.setTag("Directions", dirList);
	}

	public static class DirectionState
	{
		public EnumFacing direction;
		public boolean shouldPropagate;
		public int delay;
		public boolean inversed;
		public boolean propagated;

		public DirectionState(EnumFacing direction, boolean shouldPropagate, int delay, boolean inversed)
		{
			this.direction = direction;
			update(shouldPropagate, delay, inversed);
		}

		public DirectionState(EnumFacing direction)
		{
			this(direction, false, 0, false);
		}

		public void update(boolean shouldPropagate, int delay, boolean inversed)
		{
			this.shouldPropagate = shouldPropagate;
			this.delay = delay;
			this.inversed = inversed;
		}

		public void resetPropagationState()
		{
			this.propagated = false;
		}

		public boolean propagateState(World world, BlockPos pos, int timer, boolean powered)
		{
			if (!shouldPropagate || propagated || timer < delay)
				return false;

			IBlockState state = world.getBlockState(pos.offset(direction));
			if (state.getBlock() instanceof VanishingBlock)
				((VanishingBlock) state.getBlock()).setPowerState(world, pos.offset(direction), inversed ? !powered : powered);
			propagated = true;

			return false;
		}

		public void copy(DirectionState state)
		{
			shouldPropagate = state.shouldPropagate;
			delay = state.delay;
			inversed = state.inversed;
		}

		public void readFromNBT(NBTTagCompound nbt)
		{
			update(nbt.getBoolean("shouldPropagate"), nbt.getInteger("delay"), nbt.getBoolean("inversed"));
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt)
		{
			nbt.setInteger("direction", direction.ordinal());
			nbt.setBoolean("shouldPropagate", shouldPropagate);
			nbt.setInteger("delay", delay);
			nbt.setBoolean("inversed", inversed);

			return nbt;
		}
	}
}
