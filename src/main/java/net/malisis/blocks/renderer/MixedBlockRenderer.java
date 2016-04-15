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

package net.malisis.blocks.renderer;

import java.util.List;

import javax.vecmath.Matrix4f;

import net.malisis.blocks.MalisisBlocksSettings;
import net.malisis.blocks.block.MixedBlock;
import net.malisis.blocks.item.MixedBlockBlockItem;
import net.malisis.blocks.tileentity.MixedBlockTileEntity;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class MixedBlockRenderer extends MalisisRenderer<MixedBlockTileEntity>
{
	private IBlockState mixedBlockState;
	private Shape shape;
	private Shape simpleShape;
	private Shape[][] shapes;
	private RenderParameters rp;
	private IBlockState state1;
	private IBlockState state2;

	@Override
	protected void initialize()
	{
		simpleShape = new Cube();

		shapes = new Shape[][] { new Shape[6], new Shape[6] };
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			Shape s0 = new Cube();
			Shape s1 = new Cube();
			s0.enableMergedVertexes();
			s1.enableMergedVertexes();
			shapes[0][dir.ordinal()] = s0.removeFace(s0.getFace(Face.nameFromDirection(dir))).storeState();
			shapes[1][dir.ordinal()] = s1.shrink(dir, 0.999F).removeFace(s1.getFace(Face.nameFromDirection(dir))).storeState();
		}

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.usePerVertexAlpha.set(true);
		rp.useWorldSensitiveIcon.set(false);
	}

	private boolean setup()
	{
		if (renderType == RenderType.ITEM)
		{
			if (!itemStack.hasTagCompound())
				return false;

			Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(itemStack.getTagCompound());
			state1 = pair.getLeft();
			state2 = pair.getRight();

			mixedBlockState = ((MixedBlock) block).getDefaultState().withProperty(DirectionalComponent.ALL, EnumFacing.SOUTH);
		}
		else if (renderType == RenderType.BLOCK)
		{
			tileEntity = super.tileEntity;
			if (tileEntity == null)
				return false;
			state1 = tileEntity.getState1();
			state2 = tileEntity.getState2();

			mixedBlockState = blockState;
		}

		if (state1 == null || state2 == null)
			return false;
		return true;
	}

	@Override
	public Matrix4f getTransform(Item item, TransformType tranformType)
	{
		return DefaultRenderer.block.getTransform(item, tranformType);
	}

	@Override
	public void render()
	{
		if (!setup())
			return;

		if (renderType == RenderType.ITEM)
		{
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
			GlStateManager.enableColorMaterial();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			enableBlending();
		}

		if (MalisisBlocksSettings.simpleMixedBlockRendering.get() || !Minecraft.getMinecraft().gameSettings.fancyGraphics)
		{
			renderSimple();
			return;
		}

		set(state1);
		drawPass(true);
		set(state2);
		drawPass(false);
	}

	private void setColor()
	{
		rp.colorMultiplier.set(block instanceof BlockGrass ? 0xFFFFFF : colorMultiplier(world, pos, blockState));
	}

	private void renderSimple()
	{
		boolean reversed = false;
		float width = 1;
		float height = 1;
		float depth = 1;
		float offsetX = 0;
		float offestY = 0;
		float offsetZ = 0;
		EnumFacing dir = DirectionalComponent.getDirection(mixedBlockState);

		if (dir == EnumFacing.DOWN || dir == EnumFacing.UP)
		{
			height = 0.5F;
			offestY = 0.5F;
			if (dir == EnumFacing.UP)
				reversed = true;
		}
		if (dir == EnumFacing.WEST || dir == EnumFacing.EAST)
		{
			width = 0.5F;
			offsetX = 0.5F;
			if (dir == EnumFacing.EAST)
				reversed = true;
		}
		if (dir == EnumFacing.NORTH || dir == EnumFacing.SOUTH)
		{
			depth = 0.5F;
			offsetZ = 0.5F;
			if (dir == EnumFacing.SOUTH)
				reversed = true;
		}

		shape = simpleShape;
		set(reversed ? state2 : state1);
		shape.resetState().setSize(width, height, depth);
		rp.icon.set(new VanillaIcon(blockState));
		setColor();
		drawShape(shape, rp);

		set(reversed ? state1 : state2);
		shape.resetState().setSize(width, height, depth).translate(offsetX, offestY, offsetZ);
		rp.icon.set(new VanillaIcon(blockState));
		setColor();
		drawShape(shape, rp);
	}

	private void drawPass(boolean firstBlock)
	{
		EnumFacing dir = DirectionalComponent.getDirection(mixedBlockState);
		if (firstBlock)
			dir = dir.getOpposite();

		shape = shapes[firstBlock && renderType == RenderType.BLOCK ? 1 : 0][dir.ordinal()];
		shape.resetState();

		if (shouldShadeFace(firstBlock))
		{
			List<MergedVertex> vertexes = shape.getMergedVertexes(dir);
			for (MergedVertex v : vertexes)
				v.setAlpha(0);
		}

		rp.icon.set(new VanillaIcon(blockState));
		setColor();

		drawShape(shape, rp);
	}

	protected boolean shouldShadeFace(Boolean firstBlock)
	{
		if (block.canRenderInLayer(blockState, BlockRenderLayer.TRANSLUCENT) || block.canRenderInLayer(blockState, BlockRenderLayer.CUTOUT)
				|| block.canRenderInLayer(blockState, BlockRenderLayer.CUTOUT_MIPPED))
			return true;

		IBlockState other = firstBlock ? state2 : state1;
		if (other.getBlock().canRenderInLayer(blockState, BlockRenderLayer.TRANSLUCENT)
				|| other.getBlock().canRenderInLayer(blockState, BlockRenderLayer.CUTOUT)
				|| other.getBlock().canRenderInLayer(blockState, BlockRenderLayer.CUTOUT_MIPPED))
			return true;

		return !firstBlock;
	}

	@Override
	protected boolean shouldRenderFace(Face face, RenderParameters params)
	{
		if (renderType != RenderType.BLOCK || world == null || block == null)
			return true;
		if (params != null && params.renderAllFaces.get())
			return true;

		RenderParameters p = face.getParameters();
		if (p.direction.get() == null)
			return true;

		return mixedBlockState.shouldSideBeRendered(world, pos.offset(p.direction.get()), p.direction.get());
	}
}
