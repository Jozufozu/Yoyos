package com.jozufozu.yoyos.common;

import com.jozufozu.yoyos.common.yotools.YoToolData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;

public class CommandDebugYoyo extends CommandBase
{
    @Override
    public String getName()
    {
        return "yoyo";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/yoyo ify";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        Entity commandSenderEntity = sender.getCommandSenderEntity();

        if (commandSenderEntity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) commandSenderEntity;

            YoToolData.applyYoToolNBT(player.getHeldItem(EnumHand.MAIN_HAND));
        }
    }
}
