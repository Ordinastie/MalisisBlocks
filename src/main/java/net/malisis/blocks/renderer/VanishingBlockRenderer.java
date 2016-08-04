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

import java.util.Random;

import net.malisis.blocks.MalisisBlocksSettings;
import net.malisis.blocks.ProxyAccess;
import net.malisis.blocks.tileentity.VanishingTileEntity;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class VanishingBlockRenderer extends MalisisRenderer<VanishingTileEntity>
{
	private Shape cube = new Cube();
	public Random rand = new Random();

	public VanishingBlockRenderer()
	{
		registerFor(VanishingTileEntity.class);
	}

	@Override
	public void render()
	{
		if (tileEntity == null)
			return;

		cube.resetState();
		if (renderType == RenderType.TILE_ENTITY)
			renderVanishingTileEntity();
		if (renderType == RenderType.BLOCK)
			renderVanishingBlock();
	}

	private void renderVanishingBlock()
	{
		if (tileEntity.isPowered() || tileEntity.isInTransition() || tileEntity.isVibrating())
		{
			return;
		}

		tileEntity.blockDrawn = true;

		if (tileEntity.getCopiedState() == null)
		{
			if (getRenderLayer() == BlockRenderLayer.CUTOUT_MIPPED)
				drawShape(cube);
			return;
		}

		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		//wr.setVertexFormat(DefaultVertexFormats.BLOCK);
		try
		{
			if (tileEntity.getCopiedState().getBlock().canRenderInLayer(blockState, getRenderLayer()))
			{
				if (tileEntity.getCopiedState().getBlock() instanceof MalisisBlock)
					vertexDrawn |= blockRenderer.renderBlock(tileEntity.getCopiedState(), pos, ProxyAccess.get(world), buffer);
				else
				{
					IBlockState copiedState = tileEntity.getCopiedState()
														.getBlock()
														.getExtendedState(tileEntity.getCopiedState(), ProxyAccess.get(world), pos);
					IBakedModel model = blockRenderer.getModelForState(tileEntity.getCopiedState());
					vertexDrawn |= blockRenderer.getBlockModelRenderer().renderModel(ProxyAccess.get(world),
							model,
							copiedState,
							pos,
							buffer,
							true);
				}
			}
		}
		catch (Exception e)
		{
			drawShape(cube);
		}
	}

	private void renderVanishingTileEntity()
	{
		tileEntity = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (tileEntity == null)
			return;

		enableBlending();

		float fx = 0.0F;
		float fy = 0.0F;
		float fz = 0.0F;
		float scale = (float) (tileEntity.getDuration() - tileEntity.getTransitionTimer()) / tileEntity.getDuration();
		boolean rendered = tileEntity.getCopiedState() != null;

		RenderParameters rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.interpolateUV.set(false);

		// randomize position for vibrations
		if (tileEntity.isVibrating())
		{
			rp.alpha.set(200);
			fx = rand.nextFloat() * 0.05F;
			fy = rand.nextFloat() * 0.05F;
			fz = rand.nextFloat() * 0.05F;
			if (rand.nextBoolean())
				GL11.glTranslated(fx, fy, fz);
			else
				GL11.glRotatef(rand.nextInt(5), 1, 1, 1);
		}
		else if (tileEntity.isInTransition())
		{
			int alpha = tileEntity.getCopiedState() != null ? 255 - (int) (scale * 255) : (int) (scale * 255);
			rp.alpha.set(alpha);
			cube.scale(scale - 0.001F);
		}
		else if (tileEntity.blockDrawn)
			return;

		//no state copied, draw regular cube model for the Vanishing Block
		if (tileEntity.getCopiedState() == null)
		{
			drawShape(cube, rp);
			return;
		}

		next(DefaultVertexFormats.BLOCK);
		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		//wr.setVertexFormat(DefaultVertexFormats.BLOCK);
		try
		{
			boolean smbr = MalisisBlocksSettings.simpleMixedBlockRendering.get();
			MalisisBlocksSettings.simpleMixedBlockRendering.set(true);

			BlockPos translate = BlockPosUtils.chunkPosition(pos);
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			GlStateManager.scale(scale, scale, scale);
			if (tileEntity.getCopiedState().getBlock() instanceof MalisisBlock) //assume MalisisBlock is MalisisRendered
				GlStateManager.translate(-translate.getX(), -translate.getY(), -translate.getZ());
			else
				GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);

			GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
			//				GL11.glAlphaFunc(GL11.GL_GREATER, 1F);
			//GL14.glBlendColor(0, 0, 0, 1 - scale);
			//TODO: render underlying model with vanishing block texture
			//				renderBlocks.overrideBlockTexture = block.getIcon(blockMetadata, 0);
			//				rendered = renderBlocks.renderBlockByRenderType(tileEntity.copiedBlock, x, y, z);
			//				renderBlocks.overrideBlockTexture = null;
			//				next();

			GL14.glBlendColor(0, 0, 0, scale);
			for (BlockRenderLayer layer : BlockRenderLayer.values())
			{
				if (!tileEntity.getCopiedState().getBlock().canRenderInLayer(blockState, layer))
					continue;

				ForgeHooksClient.setRenderLayer(layer);
				if (layer == BlockRenderLayer.TRANSLUCENT)
					GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				if (tileEntity.getCopiedState().getBlock() instanceof MalisisBlock)
				{
					rendered |= blockRenderer.renderBlock(tileEntity.getCopiedState(), pos, ProxyAccess.get(world), buffer);
					//drawShape(cube, rp);
				}
				else
				{
					IBlockState copiedState = tileEntity.getCopiedState()
														.getBlock()
														.getExtendedState(tileEntity.getCopiedState(), ProxyAccess.get(world), pos);
					IBakedModel model = blockRenderer.getModelForState(tileEntity.getCopiedState());
					vertexDrawn |= blockRenderer.getBlockModelRenderer().renderModel(ProxyAccess.get(world),
							model,
							copiedState,
							pos,
							buffer,
							false);
				}

				next();
			}

			if (!rendered)
				drawShape(cube, rp);

			GlStateManager.popMatrix();

			if (tileEntity.getCopiedTileEntity() != null)
			{
				TileEntity te = tileEntity.getCopiedTileEntity();
				clean();
				TileEntityRendererDispatcher.instance.renderTileEntity(te, partialTick, 0);
			}

			MalisisBlocksSettings.simpleMixedBlockRendering.set(smbr);

		}
		catch (Exception e)
		{
			System.out.println("Fail to render " + tileEntity.getCopiedState());
			e.printStackTrace();

			drawShape(cube, rp);
		}
	}
}
