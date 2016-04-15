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
import net.malisis.blocks.MalisisBlocks.Sounds;
import net.malisis.blocks.tileentity.SwapperTileEntity;
import net.malisis.core.MalisisCore;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.PowerComponent;
import net.malisis.core.block.component.PowerComponent.Type;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class Swapper extends MalisisBlock implements ITileEntityProvider
{
	public static PropertyBool POWERED = PropertyBool.create("powered");

	public Swapper()
	{
		super(Material.IRON);
		setCreativeTab(MalisisBlocks.tab);
		setHardness(3.0F);
		setName("swapper");

		addComponent(new DirectionalComponent(DirectionalComponent.ALL));
		addComponent(new PowerComponent(Type.REDSTONE));

		if (MalisisCore.isClient())
		{
			addComponent(IIconProvider.create(MalisisBlocks.modid + ":blocks/", "swapper")
										.withSide(EnumFacing.SOUTH, "swapper_top")
										.build());
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
	{
		if (world.isRemote)
			return;

		boolean powered = world.isBlockIndirectlyGettingPowered(pos) != 0;
		if (PowerComponent.isPowered(state) != powered)
		{
			world.playSound(null, pos, Sounds.portal, SoundCategory.BLOCKS, 0.3F, 0.5F);
			if (world.isRemote)
				return;
			world.setBlockState(pos, state.withProperty(PowerComponent.getProperty(this), powered));
			SwapperTileEntity te = TileEntityUtils.getTileEntity(SwapperTileEntity.class, world, pos);
			if (te != null)
				te.swap();
		}
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new SwapperTileEntity();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		SwapperTileEntity te = TileEntityUtils.getTileEntity(SwapperTileEntity.class, world, pos);
		if (te == null)
			return;
		te.dropStoredStates();
	}

	//	@SideOnly(Side.CLIENT)
	//	public static class SwapperIconProvider implements IBlockIconProvider
	//	{
	//		private Icon icon = new Icon(MalisisBlocks.modid + ":blocks/swapper");
	//		private Icon top = new Icon(MalisisBlocks.modid + ":blocks/swapper_top");
	//
	//		@Override
	//		public void registerIcons(TextureMap textureMap)
	//		{
	//			icon = icon.register(textureMap);
	//			top = top.register(textureMap);
	//		}
	//
	//		@Override
	//		public Icon getIcon()
	//		{
	//			return icon;
	//		}
	//
	//		@Override
	//		public Icon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	//		{
	//			return side == EnumFacing.SOUTH ? top : icon;
	//		}
	//
	//		@Override
	//		public Icon getIcon(ItemStack itemStack, EnumFacing side)
	//		{
	//			return side == EnumFacing.UP ? top : icon;
	//		}
	//	}
}
