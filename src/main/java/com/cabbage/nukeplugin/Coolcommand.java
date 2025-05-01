package com.cabbage.nukeplugin;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Coolcommand implements CommandExecutor {

    private final NukePlugin plugin;

    public Coolcommand(NukePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command,  String label,  String[] args) {


        return false;
    }
}
