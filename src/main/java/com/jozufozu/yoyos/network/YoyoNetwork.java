/*
 * Copyright (c) 2018 Jozsef Augusztiny
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.Yoyos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class YoyoNetwork
{
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Yoyos.MODID);
    
    private static int ID = 0;
    
    public static void initialize()
    {
        registerMessage(MessageYoyoRetracting.class, MessageYoyoRetracting.Handler.class, Side.SERVER);
        registerMessage(MessageYoyoRetracting.MessageYoyoRetractingReply.class, MessageYoyoRetracting.MessageYoyoRetractingReply.Handler.class, Side.CLIENT);

        registerMessage(MessageCollectedDrops.class, MessageCollectedDrops.Handler.class, Side.CLIENT);

        registerMessage(MessageReelState.class, MessageReelState.Handler.class, Side.SERVER);
        registerMessage(MessageReelState.MessageReelStateReply.class, MessageReelState.MessageReelStateReply.Handler.class, Side.CLIENT);

        registerMessage(MessageAcquireTarget.class, MessageAcquireTarget.Handler.class, Side.SERVER);
        registerMessage(MessageAcquireTarget.MessageTargetUpdate.class, MessageAcquireTarget.MessageTargetUpdate.Handler.class, Side.CLIENT);
    }
    
    private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<REQ> requestMessageType, Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Side side)
    {
        INSTANCE.registerMessage(messageHandler, requestMessageType, ID++, side);
    }
}
