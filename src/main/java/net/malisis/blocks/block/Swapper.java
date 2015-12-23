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
import net.malisis.blocks.tileentity.SwapperTileEntity;
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class Swapper extends MalisisBlock implements ITileEntityProvider, IBlockDirectional
{
	public static PropertyBool POWERED = PropertyBool.create("powered");

	public Swapper()
	{
		super(Material.iron);
		setCreativeTab(MalisisBlocks.tab);
		setHardness(3.0F);
		setName("swapper");
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, POWERED, IBlockDirectional.ALL);
	}

	@Override
	public PropertyDirection getPropertyDirection()
	{
		return ALL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = new SwapperIconProvider();
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
	{
		if (world.isRemote)
			return;

		boolean powered = world.isBlockIndirectlyGettingPowered(pos) != 0;
		if (isPowered(state) != powered)
		{
			world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, MalisisBlocks.modid + ":portal", 0.3F, 0.5F);
			if (world.isRemote)
				return;
			world.setBlockState(pos, state.withProperty(POWERED, powered));
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
	public IBlockState getStateFromMeta(int meta)
	{
		return super.getStateFromMeta(meta).withProperty(POWERED, (meta >> 3) != 0);
		//return getDefaultState().withProperty(POWERED, meta != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return super.getMetaFromState(state) + (isPowered(state) ? (1 << 3) : 0);
		//return isPowered(state) ? 1 : 0;
	}

	public boolean isPowered(World world, BlockPos pos)
	{
		return isPowered(world.getBlockState(pos));
	}

	public boolean isPowered(IBlockState state)
	{
		return state.getBlock() == this && (boolean) state.getValue(POWERED);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		SwapperTileEntity te = TileEntityUtils.getTileEntity(SwapperTileEntity.class, world, pos);
		if (te == null)
			return;
		te.dropStoredStates();
	}

	@SideOnly(Side.CLIENT)
	public static class SwapperIconProvider implements IBlockIconProvider
	{
		private MalisisIcon icon = new MalisisIcon(MalisisBlocks.modid + ":blocks/swapper");
		private MalisisIcon top = new MalisisIcon(MalisisBlocks.modid + ":blocks/swapper_top");

		@Override
		public void registerIcons(TextureMap textureMap)
		{
			icon = icon.register(textureMap);
			top = top.register(textureMap);
		}

		@Override
		public MalisisIcon getIcon()
		{
			return icon;
		}

		@Override
		public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
		{
			return IBlockDirectional.getDirection(state) == side ? top : icon;
		}

		@Override
		public MalisisIcon getIcon(ItemStack itemStack, EnumFacing side)
		{
			return side == EnumFacing.UP ? top : icon;
		}
	}
}
