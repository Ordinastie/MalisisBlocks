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

package net.malisis.blocks.item;

import com.google.common.eventbus.Subscribe;

import net.malisis.blocks.MalisisBlocks;
import net.malisis.blocks.renderer.VanishingCopierRenderer;
import net.malisis.blocks.vanishingoption.VanishingOptions;
import net.malisis.blocks.vanishingoption.VanishingOptionsGui;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDeferredInventoryProvider;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.item.MalisisItem;
import net.malisis.core.renderer.MalisisRendered;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(VanishingCopierRenderer.class)
public class VanishingCopierItem extends MalisisItem implements IDeferredInventoryProvider<ItemStack>
{
	public VanishingCopierItem()
	{
		setName("vanishingCopier");
		setCreativeTab(MalisisBlocks.tab);
		setMaxDamage(0);
		setTexture(MalisisBlocks.modid + ":items/vanishing_copier");
	}

	public VanishingOptions getVanishingOptions(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
			itemStack.setTagCompound(new NBTTagCompound());
		VanishingOptions vanishingOptions = new VanishingOptions(itemStack);
		vanishingOptions.readFromNBT(itemStack.getTagCompound());
		vanishingOptions.getSlot().register(this);
		return vanishingOptions;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack itemStack = player.getHeldItem(hand);
		if (world.isRemote || hand == EnumHand.OFF_HAND)
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);

		MalisisInventory.open((EntityPlayerMP) player, this, itemStack);

		return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entityIn, int itemSlot, boolean isSelected)
	{
		if (!world.isRemote)
			return;

		//		if (MalisisGui.currentGui() instanceof VanishingOptionsGui)
		//			MalisisGui.currentGui().updateGui();
	}

	@Override
	public MalisisInventory getInventory(ItemStack itemStack)
	{
		return getVanishingOptions(itemStack).getInventory();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(ItemStack itemStack, MalisisInventoryContainer container)
	{
		return new VanishingOptionsGui(getVanishingOptions(itemStack), container);
	}

	@Subscribe
	public void onSlotChanged(InventoryEvent.SlotChanged event)
	{
		VanishingOptions vanishingOptions = ((VanishingOptions) event.getInventory().getProvider());
		vanishingOptions.save();
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return 1;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack itemStack)
	{
		return 1;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack itemStack)
	{
		ItemStack is = getVanishingOptions(itemStack).getSlot().getItemStack();
		if (is == null)
			return 1;
		return 1 - ((double) is.getCount() / is.getMaxStackSize());
	}
}
