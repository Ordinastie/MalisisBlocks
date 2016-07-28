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

import java.util.List;

import net.malisis.blocks.item.MixedBlockBlockItem;
import net.malisis.blocks.renderer.MixedBlockRenderer;
import net.malisis.blocks.tileentity.MixedBlockTileEntity;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;

@MalisisRendered(MixedBlockRenderer.class)
public class MixedBlock extends MalisisBlock implements ITileEntityProvider
{
	public MixedBlock()
	{
		super(Material.ROCK);
		setName("mixed_block");
		setHardness(0.7F);

		addComponent(new DirectionalComponent(DirectionalComponent.ALL));
	}

	@Override
	public Item getItem(Block block)
	{
		return new MixedBlockBlockItem(this);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null)
			return;
		te.set(itemStack);
		world.notifyBlockOfStateChange(pos, this);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null)
			return null;
		return MixedBlockBlockItem.fromTileEntity(te);
	}

	@SuppressWarnings("deprecation")
	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return 0;

		return Math.max(te.getState1().getLightValue(), te.getState2().getLightValue());
	}

	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return 0;

		return te.getState1().getBlock() == Blocks.REDSTONE_BLOCK || te.getState2().getBlock() == Blocks.REDSTONE_BLOCK ? 15 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager)
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(target.getBlockPos());
		if (te == null || te.getState1() == null || te.getState2() == null)
			return true;

		EntityUtils.addHitEffects(world, target, manager, te.getState1(), te.getState2());

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager)
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return true;

		EntityUtils.addDestroyEffects(world, pos, manager, te.getState1(), te.getState2());

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new MixedBlockTileEntity();
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if (!player.capabilities.isCreativeMode)
		{
			MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
			if (te != null)
				spawnAsEntity(world, pos, MixedBlockBlockItem.fromTileEntity(te));
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return Lists.newArrayList();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer)
	{
		return layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		IBlockState neighborState = world.getBlockState(pos.offset(side));
		if (neighborState.getMaterial() == Material.AIR)
			return true;

		if (neighborState.getBlock() != this && !(neighborState.getBlock() instanceof BlockBreakable))
			return !neighborState.isOpaqueCube();

		MixedBlockTileEntity current = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		return current != null && !isOpaque(world, pos) && current.isOpaque();
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
	{
		return isOpaque(world, pos);
	}

	public static boolean isOpaque(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockBreakable)
			return true;

		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		return te != null && te.isOpaque();
	}
}
