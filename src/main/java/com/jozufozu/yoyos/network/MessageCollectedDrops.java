package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.common.EntityYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageCollectedDrops implements IMessage
{
    private int yoyoID;
    private int[] dropIDs;
    
    public MessageCollectedDrops() {}
    
    public MessageCollectedDrops(EntityYoyo yoyo)
    {
        this.yoyoID = yoyo.getEntityId();
        this.dropIDs = new int[yoyo.collectedDrops.size()];
        for (int i = 0; i < yoyo.collectedDrops.size(); i++)
            this.dropIDs[i] = yoyo.collectedDrops.get(i).getEntityId();
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.yoyoID = buf.readInt();
        int length = buf.readInt();
    
        this.dropIDs = new int[length];
        for (int i = 0; i < length; i++)
            this.dropIDs[i] = buf.readInt();
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(yoyoID);
        buf.writeInt(dropIDs.length);
        for (int dropID : dropIDs)
        {
            buf.writeInt(dropID);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageCollectedDrops, IMessage>
    {
        @Override
        public IMessage onMessage(MessageCollectedDrops message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                Minecraft mc = Minecraft.getMinecraft();
                
                Entity maybeYoYo = mc.world.getEntityByID(message.yoyoID);
                
                if (maybeYoYo != null && maybeYoYo instanceof EntityYoyo)
                {
                    EntityYoyo yoyo = (EntityYoyo) maybeYoYo;
    
                    yoyo.collectedDrops.clear();
                    for (int dropID : message.dropIDs)
                    {
                        Entity maybeItem = mc.world.getEntityByID(dropID);
    
                        if (maybeItem != null && maybeItem instanceof EntityItem)
                        {
                            yoyo.collectedDrops.add(((EntityItem) maybeItem));
                        }
                    }
                }
            });
            return null;
        }
    }
}
