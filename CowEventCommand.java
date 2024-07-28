package com.event.cowevent;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CowEventCommand implements CommandExecutor {

    private final CowEvent plugin;

    public CowEventCommand(CowEvent plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /cowevent <start|stop|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                plugin.startEvent();
                sender.sendMessage("§aThe event has been started.");
                break;
            case "stop":
                plugin.stopEvent();
                sender.sendMessage("§cThe event has been stopped.");
                break;
            case "reload":
                plugin.loadConfig();
                sender.sendMessage("§aConfiguration has been reloaded.");
                break;
            default:
                sender.sendMessage("§cUnknown command. Use: /cowevent <start|stop|reload>");
                break;
        }

        return true;
    }
}
