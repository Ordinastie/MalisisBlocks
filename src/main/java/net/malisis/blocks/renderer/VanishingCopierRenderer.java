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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.malisis.blocks.MalisisBlocks;
import net.malisis.blocks.MalisisBlocks.Items;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.renderer.model.loader.TextureModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.TRSRTransformation;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class VanishingCopierRenderer extends DefaultRenderer.Item
{

	private AnimationRenderer ar = new AnimationRenderer();
	private Shape shape;

	private Matrix4f thirdPerson = new TRSRTransformation(new Vector3f(0.05F, 0.0F, -0.195F),
			TRSRTransformation.quatFromYXZDegrees(new Vector3f(120, -145, 00)), new Vector3f(0.55F, 0.55F, 0.55F), null).getMatrix();
	private Matrix4f firstPerson = new TRSRTransformation(new Vector3f(0, 0.280F, 0.14F),
			TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, -135, 0)), new Vector3f(1.7F, 1.7F, 1.7F), null).getMatrix();

	@Override
	public void initialize()
	{
		super.initialize();
		MalisisIcon icon = MalisisBlocks.Items.vanishingCopierItem.getIconProvider().getIcon();
		MalisisModel model = new MalisisModel(new TextureModelLoader(icon));
		shape = model.getShape("shape");
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		if (tranformType == TransformType.FIRST_PERSON)
			return firstPerson;
		else if (tranformType == TransformType.THIRD_PERSON)
			return thirdPerson;

		return null;
	}

	@Override
	public void render()
	{
		super.render();

		//		if (tranformType != TransformType.FIRST_PERSON)
		//			return;

		ItemStack copiedStack = Items.vanishingCopierItem.getVanishingOptions(itemStack).getSlot().getItemStack();
		if (copiedStack == null)
			return;

		draw();

		byte count = 1;
		if (tranformType != TransformType.GUI)
		{
			if (copiedStack.stackSize > 48)
				count = 5;
			else if (copiedStack.stackSize > 32)
				count = 4;
			else if (copiedStack.stackSize > 16)
				count = 3;
			else if (copiedStack.stackSize > 1)
				count = 2;
		}

		GlStateManager.translate(.2F, 0.75F, 0.5F);
		GlStateManager.scale(0.35F, 0.35F, 0.35F);
		if (tranformType != TransformType.GUI)
			GlStateManager.rotate(360 * ar.getElapsedTime() / 3000, 1, 1, 1);

		Random rand = new Random();
		rand.setSeed(187L);

		for (int j = 0; j < count; ++j)
		{
			if (count > 0)
			{
				float rx = (rand.nextFloat() * 2.0F - 1.0F) * 0.15F;
				float ry = (rand.nextFloat() * 2.0F - 1.0F) * 0.15F;
				float rz = (rand.nextFloat() * 2.0F - 1.0F) * 0.15F;
				GlStateManager.translate(rx, ry, rz);
			}

			Minecraft.getMinecraft().getRenderItem().renderItemModel(copiedStack);
		}
	}

	@Override
	protected Shape getModelShape()
	{
		return shape;
	}
}
