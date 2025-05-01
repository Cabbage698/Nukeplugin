package com.cabbage.nukeplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class NukePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Register commands
        getCommand("fire").setExecutor(new FireCommand(this));
        getCommand("cabbage").setExecutor(new CabbageCommand(this));
        getLogger().info("NukePlugin has been enabled!");
        {
            getLogger().info("IM AWAKE!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("NukePlugin has been disabled!");
    }
    {
        getLogger().info("Im going to sleep");
    }

}