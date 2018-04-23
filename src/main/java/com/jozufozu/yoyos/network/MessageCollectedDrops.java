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

import javax.annotation.Nullable;
import java.util.Collections;

public class MessageCollectedDrops implements IMessage
{
    private int yoyoID;
    private ItemStack[] drops;
    
    public MessageCollectedDrops() {}
    
    public MessageCollectedDrops(EntityYoyo yoyo)
    {
        yoyoID = yoyo.getEntityId();
        drops = new ItemStack[yoyo.collectedDrops.size()];
        for (int i = 0; i < yoyo.collectedDrops.size(); i++)
            drops[i] = yoyo.collectedDrops.get(i);
    }
    
    @Override
    public void fromBytes(ByteBuf buf)
    {
        yoyoID = buf.readInt();
        int length = buf.readInt();
    
        drops = new ItemStack[length];
        for (int i = 0; i < length; i++)
            drops[i] = ByteBufUtils.readItemStack(buf);
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
        @Nullable
        public IMessage onMessage(MessageCollectedDrops message, MessageContext ctx)
        {
            Minecraft.getMinecraft().addScheduledTask(() ->
            {
                Minecraft mc = Minecraft.getMinecraft();
                
                Entity maybeYoYo = mc.world.getEntityByID(message.yoyoID);
                
                if (maybeYoYo instanceof EntityYoyo)
                {
                    EntityYoyo yoyo = (EntityYoyo) maybeYoYo;

                    // We trust that the drops are condensed
                    yoyo.collectedDrops.clear();
                    Collections.addAll(yoyo.collectedDrops, message.drops);
                }
            });
            return null;
        }
    }
}
