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

package net.malisis.blocks.network;

import io.netty.buffer.ByteBuf;
import net.malisis.blocks.MalisisBlocks;
import net.malisis.blocks.network.SwapperMessage.Packet;
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class SwapperMessage implements IMalisisMessageHandler<Packet, IMessage>
{
	public SwapperMessage()
	{
		MalisisBlocks.network.registerMessage(this, SwapperMessage.Packet.class, Side.CLIENT);
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		World world = IMalisisMessageHandler.getWorld(ctx);
		IBlockState state = Block.getStateById(message.stateId);
		world.setBlockState(message.pos, state);
		world.getTileEntity(message.pos).readFromNBT(message.tag);
	}

	public static void sendTileEntityTag(TileEntity tileEntity)
	{
		MalisisBlocks.network.sendToPlayersWatchingChunk(new Packet(tileEntity),
				tileEntity.getWorld().getChunkFromBlockCoords(tileEntity.getPos()));
	}

	public static class Packet implements IMessage
	{
		BlockPos pos;
		int stateId;
		NBTTagCompound tag;

		public Packet()
		{}

		public Packet(TileEntity te)
		{
			this.pos = te.getPos();
			this.stateId = Block.getStateId(te.getWorld().getBlockState(te.getPos()));
			this.tag = te.getUpdateTag();
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			pos = BlockPos.fromLong(buf.readLong());
			stateId = buf.readInt();
			tag = ByteBufUtils.readTag(buf);
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeLong(pos.toLong());
			buf.writeInt(stateId);
			ByteBufUtils.writeTag(buf, tag);
		}
	}
}
