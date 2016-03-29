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

package net.malisis.blocks.block;

import net.malisis.blocks.MalisisBlocks;
import net.malisis.blocks.tileentity.BlockMixerTileEntity;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.renderer.icon.provider.SidesIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMixer extends MalisisBlock implements ITileEntityProvider
{
	public BlockMixer()
	{
		super(Material.iron);
		setCreativeTab(MalisisBlocks.tab);
		setHardness(3.0F);
		setName("block_mixer");

		addComponent(new DirectionalComponent());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		SidesIconProvider ip = new SidesIconProvider(MalisisBlocks.modid + ":blocks/block_mixer_side");
		ip.setSideIcon(EnumFacing.SOUTH, MalisisBlocks.modid + ":blocks/block_mixer");
		iconProvider = ip;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		if (player.isSneaking())
			return false;

		BlockMixerTileEntity te = TileEntityUtils.getTileEntity(BlockMixerTileEntity.class, world, pos);
		MalisisInventory.open((EntityPlayerMP) player, te);
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		BlockMixerTileEntity provider = TileEntityUtils.getTileEntity(BlockMixerTileEntity.class, world, pos);
		if (provider != null)
			provider.breakInventories(world, pos);
		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metdata)
	{
		return new BlockMixerTileEntity();
	}
}
