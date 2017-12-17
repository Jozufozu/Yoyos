package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.common.EntityYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageRetractYoYo implements IMessage
{
    
    private int yoyoID;
    private boolean retracting;
    
    public MessageRetractYoYo() {}
    
    public MessageRetractYoYo(EntityYoyo yoYo)
    {
        this.yoyoID = yoYo.getEntityId();
        this.retracting = yoYo.isRetracting();
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.yoyoID = buf.readInt();
        this.retracting = buf.readBoolean();
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(this.yoyoID);
        buf.writeBoolean(this.retracting);
    }
    
    public static class Handler implements IMessageHandler<MessageRetractYoYo, IMessage>
    {
        @Override
        public IMessage onMessage(MessageRetractYoYo message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                Minecraft mc = Minecraft.getMinecraft();
                
                Entity maybeYoYo = mc.world.getEntityByID(message.yoyoID);
                
                if (maybeYoYo != null && maybeYoYo instanceof EntityYoyo)
                {
                    ((EntityYoyo) maybeYoYo).setRetracting(message.retracting);
                }
            });
            return null;
        }
    }
}
