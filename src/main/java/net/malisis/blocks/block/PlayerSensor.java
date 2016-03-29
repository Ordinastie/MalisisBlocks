package net.malisis.blocks.block;

import java.util.List;
import java.util.Random;

import net.malisis.blocks.MalisisBlocks;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.DirectionalComponent.Placement;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

public class PlayerSensor extends MalisisBlock
{
	public static PropertyBool POWERED = PropertyBool.create("powered");

	public PlayerSensor()
	{
		super(Material.circuits);
		setCreativeTab(MalisisBlocks.tab);
		setName("player_sensor");
		setTexture(MalisisBlocks.modid + ":blocks/player_sensor");

		addComponent(new DirectionalComponent(DirectionalComponent.ALL, Placement.BLOCKSIDE));

		setDefaultState(getDefaultState().withProperty(POWERED, false));
	}

	@Override
	protected List<IProperty<?>> getProperties()
	{
		return Lists.newArrayList(POWERED);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		if (type == BoundingBoxType.COLLISION)
			return null;

		float f = 0.125F;
		switch (DirectionalComponent.getDirection(world, pos))
		{
			case DOWN:
				return new AxisAlignedBB(0.5F - f, 1 - f / 2, 0.5F - f, 0.5F + f, 1, 0.5F + f);
			case UP:
				return new AxisAlignedBB(0.5F - f, 0, 0.5F - f, 0.5F + f, f / 2, 0.5F + f);
			default:
				return new AxisAlignedBB(f, f, 0, 1 - f, 2 * f, f);
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		side = side.getOpposite();
		return world.isSideSolid(pos.offset(side), side);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		for (EnumFacing side : EnumFacing.values())
			if (world.isSideSolid(pos.offset(side), side))
				return true;

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		EnumFacing dir = DirectionalComponent.getDirection(world, pos).getOpposite();
		if (!world.isSideSolid(pos.offset(dir), dir))
		{
			dropBlockAsItem(world, pos, getDefaultState(), 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		if (isPowered(state))
			notifyPower(world, pos, state);
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return isPowered(state) ? 15 : 0;
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		if (isPowered(state) && DirectionalComponent.getDirection(world, pos) == side)
			return 15;
		return 0;

	}

	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		world.scheduleBlockUpdate(pos, this, 5, 0);
	}

	public AxisAlignedBB getDetectionBox(IBlockAccess world, BlockPos pos)
	{
		EnumFacing dir = DirectionalComponent.getDirection(world, pos);
		double x1 = pos.getX(), x2 = pos.getX();
		double z1 = pos.getZ(), z2 = pos.getZ();
		int yOffset = 1;
		int factor = -1;

		if (dir == EnumFacing.EAST)
		{
			x1 -= 1;
			x2 += 2;
			z2 += 1;
		}
		else if (dir == EnumFacing.WEST)
		{
			x1 -= 1;
			x2 += 2;
			z2 += 1;
		}
		else if (dir == EnumFacing.NORTH)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}
		else if (dir == EnumFacing.SOUTH)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}
		else if (dir == EnumFacing.UP)
		{
			x2 += 1;
			z2 += 1;
			factor = 1;
		}
		else if (dir == EnumFacing.DOWN)
		{
			x2 += 1;
			z2 += 1;
		}

		boolean isAir = world.isAirBlock(pos.up(factor));
		while (isAir && yOffset < 6)
			isAir = world.isAirBlock(pos.up(factor * yOffset++));

		return new AxisAlignedBB(x1, pos.getY(), z1, x2, pos.up(factor * yOffset++).getY(), z2);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		boolean powered = isPowered(state);
		if (world.isRemote)
			return;

		world.scheduleBlockUpdate(pos, this, 5, 0);

		List<EntityPlayer> list = world.getEntitiesWithinAABB(EntityPlayer.class, this.getDetectionBox(world, pos));
		boolean gettingPowered = list != null && !list.isEmpty();

		if (powered != gettingPowered)
		{
			world.setBlockState(pos, state.withProperty(POWERED, gettingPowered));
			notifyPower(world, pos, state);
		}
	}

	private void notifyPower(World world, BlockPos pos, IBlockState state)
	{
		world.notifyNeighborsOfStateChange(pos, this);
		world.notifyNeighborsOfStateChange(pos.offset(DirectionalComponent.getDirection(state).getOpposite()), this);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	public static boolean isPowered(IBlockState state)
	{
		return state.getValue(POWERED);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return super.getStateFromMeta(meta).withProperty(POWERED, (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return super.getMetaFromState(state) + (isPowered(state) ? 8 : 0);
	}
}
