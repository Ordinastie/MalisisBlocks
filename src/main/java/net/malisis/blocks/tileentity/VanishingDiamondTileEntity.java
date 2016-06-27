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

package net.malisis.blocks.tileentity;

import net.malisis.blocks.MalisisBlocks.Items;
import net.malisis.blocks.block.VanishingBlock;
import net.malisis.blocks.vanishingoption.VanishingOptions;
import net.malisis.blocks.vanishingoption.VanishingOptionsGui;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class VanishingDiamondTileEntity extends VanishingTileEntity implements IDirectInventoryProvider
{
	protected int changedPowerStateTimer;
	protected VanishingOptions vanishingOptions = new VanishingOptions();

	public VanishingDiamondTileEntity()
	{
		super(VanishingBlock.Type.DIAMOND);
		vanishingOptions.getSlot().register(this);
	}

	public VanishingOptions getVanishingOptions()
	{
		return vanishingOptions;
	}

	public void setDuration(int duration)
	{
		vanishingOptions.setDuration(duration);
	}

	@Override
	public int getDuration()
	{
		return vanishingOptions.getDuration();
	}

	public void copyOptions(ItemStack itemStack)
	{
		if (itemStack == null || itemStack.getItem() != Items.vanishingCopierItem)
			return;

		VanishingOptions isOptions = Items.vanishingCopierItem.getVanishingOptions(itemStack);
		isOptions.copy(vanishingOptions);

		if (!vanishingOptions.getSlot().isEmpty() && !isOptions.getSlot().isFull())
		{
			isOptions.getInventory().transfer(vanishingOptions.getInventory());
		}

		isOptions.save();
	}

	public void pasteOptions(ItemStack itemStack, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (itemStack == null || itemStack.getItem() != Items.vanishingCopierItem)
			return;

		VanishingOptions isOptions = Items.vanishingCopierItem.getVanishingOptions(itemStack);
		vanishingOptions.copy(isOptions);
		TileEntityUtils.notifyUpdate(this);
		ItemStack isItemStack = isOptions.getSlot().getItemStack();
		ItemStack teItemStack = vanishingOptions.getSlot().getItemStack();
		if (isItemStack == null)
			return;

		if (!applyItemStack(isItemStack, player, side, hitX, hitY, hitZ))
			return;

		isOptions.save();

		if (ItemUtils.areItemStacksStackable(isItemStack, teItemStack))
			return;

		ItemStack copy = isItemStack.copy();
		copy.stackSize = 1;
		vanishingOptions.getSlot().setItemStack(copy);
	}

	@Override
	public boolean setPowerState(boolean powered)
	{
		if (!super.setPowerState(powered))
			return false;

		changedPowerStateTimer = 0;
		vanishingOptions.setPowerState(worldObj, pos, changedPowerStateTimer, powered);
		return true;
	}

	@Override
	public MalisisInventory getInventory()
	{
		return vanishingOptions.getInventory();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new VanishingOptionsGui(vanishingOptions, container, this);
	}

	@Override
	public void update()
	{
		changedPowerStateTimer++;
		vanishingOptions.propagateState(worldObj, pos, changedPowerStateTimer, powered);
		super.update();
	}

	@Subscribe
	public void onSlotChanged(InventoryEvent.SlotChanged event)
	{
		setBlockState(event.getSlot().getItemStack(), null, EnumFacing.UP, 0.5F, 0.5F, 0.5F);
		TileEntityUtils.notifyUpdate(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		vanishingOptions.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		vanishingOptions.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		TileEntityUtils.updateGui(this);
		TileEntityUtils.notifyUpdate(this);
	}

}
