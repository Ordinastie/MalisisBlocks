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

package net.malisis.blocks.vanishingoption;

import java.util.HashMap;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.eventbus.Subscribe;

import net.malisis.blocks.network.VanishingDiamondFrameMessage;
import net.malisis.blocks.network.VanishingDiamondFrameMessage.DataType;
import net.malisis.blocks.tileentity.VanishingDiamondTileEntity;
import net.malisis.blocks.vanishingoption.VanishingOptions.DirectionState;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIInventory;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.render.BackgroundTexture.WindowBackground;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.TileEntityUtils;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class VanishingOptionsGui extends MalisisGui
{
	protected VanishingDiamondTileEntity tileEntity;
	protected VanishingOptions vanishingOptions;

	protected UITextField duration;
	protected HashMap<EnumFacing, Triple<UICheckBox, UITextField, UICheckBox>> configs = new HashMap<>();

	public VanishingOptionsGui(VanishingOptions vanishingOptions, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		this.vanishingOptions = vanishingOptions;
	}

	public VanishingOptionsGui(VanishingOptions vanishingOptions, MalisisInventoryContainer container, VanishingDiamondTileEntity tileEntity)
	{
		this(vanishingOptions, container);
		this.tileEntity = tileEntity;
	}

	@Override
	public void construct()
	{

		UIContainer<?> window = new UIContainer<>(this, "gui.vanishingoptions.title", 200, 220);
		window.setBackground(new WindowBackground(this));

		window.add(new UILabel(this, "Direction").setPosition(0, 20));
		window.add(new UILabel(this, "Delay").setPosition(55, 20));
		window.add(new UILabel(this, "Inversed").setPosition(90, 20));

		int i = 0;
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			DirectionState state = vanishingOptions.getDirectionState(dir);
			int y = i * 14 + 30;
			UICheckBox cb = new UICheckBox(this, dir.name());
			cb.setPosition(2, y).setChecked(state.shouldPropagate).register(this);
			cb.attachData(Pair.of(dir, DataType.PROPAGATION));

			UITextField textField = new UITextField(this, "" + state.delay)	.setSize(27, 0)
																			.setPosition(55, y)
																			.setEnabled(state.shouldPropagate)
																			.register(this);
			textField.attachData(Pair.of(dir, DataType.DELAY));

			UICheckBox invCb = new UICheckBox(this)	.setPosition(105, y)
													.setEnabled(state.shouldPropagate)
													.setChecked(state.inversed)
													.register(this);
			invCb.attachData(Pair.of(dir, DataType.INVERSED));

			window.add(cb);
			window.add(textField);
			window.add(invCb);

			configs.put(dir, Triple.of(cb, textField, invCb));

			i++;
		}

		UIContainer<?> cont = new UIContainer<>(this, 50, 60).setPosition(0, 40, Anchor.RIGHT);

		duration = new UITextField(this, "" + vanishingOptions.getDuration())	.setSize(30, 0)
																				.setPosition(0, 10, Anchor.CENTER)
																				.register(this);
		duration.attachData(Pair.of(null, DataType.DURATION));
		cont.add(new UILabel(this, "Duration").setPosition(0, 0, Anchor.CENTER));
		cont.add(duration);

		UIInventory inv = new UIInventory(this, inventoryContainer.getInventory(0));
		inv.setPosition(0, 40, Anchor.CENTER);
		cont.add(new UILabel(this, "Block").setPosition(0, 30, Anchor.CENTER));
		cont.add(inv);

		window.add(cont);

		UIPlayerInventory playerInv = new UIPlayerInventory(this, inventoryContainer.getPlayerInventory());
		window.add(playerInv);

		addToScreen(window);

		if (tileEntity != null)
			TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	@Subscribe
	public void onConfigChanged(ComponentEvent.ValueChange<?, ?> event)
	{
		@SuppressWarnings("unchecked")
		Pair<EnumFacing, DataType> data = (Pair<EnumFacing, DataType>) event.getComponent().getData();
		int time = event.getComponent() instanceof UITextField ? NumberUtils.toInt((String) event.getNewValue()) : 0;
		boolean checked = event.getComponent() instanceof UICheckBox ? (Boolean) event.getNewValue() : false;

		vanishingOptions.set(data.getLeft(), data.getRight(), time, checked);

		VanishingDiamondFrameMessage.sendConfiguration(tileEntity, data.getLeft(), data.getRight(), time, checked);

		updateGui();
	}

	@Override
	public void updateGui()
	{
		if (!duration.isFocused())
			duration.setText("" + vanishingOptions.getDuration());

		for (EnumFacing dir : EnumFacing.VALUES)
		{
			DirectionState state = vanishingOptions.getDirectionState(dir);
			UICheckBox cb = configs.get(dir).getLeft();
			UITextField tf = configs.get(dir).getMiddle();
			UICheckBox inv = configs.get(dir).getRight();

			tf.setEnabled(state.shouldPropagate);
			inv.setEnabled(state.shouldPropagate);

			if (!cb.isFocused())
				cb.setChecked(state.shouldPropagate);
			if (!tf.isFocused())
				tf.setText("" + state.delay);
			if (!inv.isFocused())
				inv.setChecked(state.inversed);
		}
	}
}
