package net.fap.beecloud.api.synapse;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import net.fap.beecloud.api.BeeCloudAPI;

public class SimpleCommand extends Command {

    public SimpleCommand(BeeCloudAPI api, String commandStr, String commandUsage)
    {
        super(commandStr,commandUsage);
        this.setUsage(usageMessage);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        return true;
    }
}
