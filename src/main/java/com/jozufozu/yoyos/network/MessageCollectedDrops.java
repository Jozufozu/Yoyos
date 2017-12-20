package com.jozufozu.yoyos.network;

import com.jozufozu.yoyos.common.EntityYoyo;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Collections;

public class MessageCollectedDrops implements IMessage
{
    private int yoyoID;
    private ItemStack[] drops;
    
    public MessageCollectedDrops() {}
    
    public MessageCollectedDrops(EntityYoyo yoyo)
    {
        this.yoyoID = yoyo.getEntityId();
        this.drops = new ItemStack[yoyo.collectedDrops.size()];
        for (int i = 0; i < yoyo.collectedDrops.size(); i++)
            this.drops[i] = yoyo.collectedDrops.get(i);
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        this.yoyoID = buf.readInt();
        int length = buf.readInt();
    
        this.drops = new ItemStack[length];
        for (int i = 0; i < length; i++)
            this.drops[i] = ByteBufUtils.readItemStack(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(yoyoID);
        buf.writeInt(drops.length);
    
        for (ItemStack drop : drops)
            ByteBufUtils.writeItemStack(buf, drop);
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
                    Collections.addAll(yoyo.collectedDrops, message.drops);
                }
            });
            return null;
        }
    }
}
