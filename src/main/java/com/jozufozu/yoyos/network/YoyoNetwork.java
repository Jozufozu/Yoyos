package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.Yoyos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class YoyoNetwork {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Yoyos.MODID);

    private static int ID = 0;

    public static void initialize() {
        registerMessage(MessageRetractYoYo.class, MessageRetractYoYo.Handler.class, Side.CLIENT);
    }

    public static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<REQ> requestMessageType, Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Side side) {
        INSTANCE.registerMessage(messageHandler, requestMessageType, ID++, side);
    }
}
