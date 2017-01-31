package net.malisis.blocks.tileentity;

import net.malisis.blocks.gui.BlockMixerGui;
import net.malisis.blocks.item.MixedBlockBlockItem;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMixerTileEntity extends TileEntity implements IDirectInventoryProvider, ITickable
{
	private MalisisInventory inventory;
	private int mixTimer = 0;
	private int mixTotalTime = 100;
	public MixerSlot firstInput;
	public MixerSlot secondInput;
	public MalisisSlot output;

	public BlockMixerTileEntity()
	{
		firstInput = new MixerSlot();
		secondInput = new MixerSlot();
		output = new MalisisSlot();
		output.setOutputSlot();
		inventory = new MalisisInventory(this, firstInput, secondInput, output);
	}

	@Override
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new BlockMixerGui(this, container);
	}

	@Override
	public void update()
	{
		ItemStack firstItemStack = firstInput.getItemStack();
		ItemStack secondItemStack = secondInput.getItemStack();
		ItemStack outputItemStack = output.getItemStack();
		if (firstItemStack.isEmpty() || secondItemStack.isEmpty())
		{
			mixTimer = 0;
			return;
		}

		ItemStack expected = MixedBlockBlockItem.fromItemStacks(firstItemStack, secondItemStack);
		if (expected.isEmpty())
		{
			mixTimer = 0;
			return;
		}

		if (!outputItemStack.isEmpty())
		{
			if (!ItemStack.areItemStackTagsEqual(outputItemStack, expected)
					|| outputItemStack.getCount() >= outputItemStack.getMaxStackSize())
			{
				mixTimer = 0;
				return;
			}
		}

		mixTimer++;

		if (mixTimer > mixTotalTime)
		{
			mixTimer = 0;
			firstInput.extract(1);
			secondInput.extract(1);
			output.insert(expected);
		}
	}

	public float getMixTimer()
	{
		return (float) mixTimer / (float) mixTotalTime;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("mixTimer", mixTimer);
		inventory.writeToNBT(tagCompound);
		return tagCompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		mixTimer = tagCompound.getInteger("mixTimer");
		inventory.readFromNBT(tagCompound);
	}

	public class MixerSlot extends MalisisSlot
	{
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return MixedBlockBlockItem.canBeMixed(itemStack);
		}
	}
}
