package com.cabbage.nukeplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CabbageCommand implements CommandExecutor, TabCompleter {
    private final NukePlugin plugin;

    public CabbageCommand(NukePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /cabbage <credits|gamerules>");
            return true;
        }


        switch (args[0].toLowerCase()) {
            case "credits":
                showCredits(sender);
                break;
            case "gamerules":
                if (args.length < 2) {
                    showGameRules(sender);
                    return true;
                }
                handleGameRules(sender, args);
                break;
            default:
                sender.sendMessage("§cUnknown subcommand! Use 'credits' or 'gamerules'");
        }
        return true;
    }

    private void showCredits(CommandSender sender) {
        sender.sendMessage("§6=== Orbital Strike Plugin Credits ===");
        sender.sendMessage("§bSpigot Page: §fhttps://www.spigotmc.org/resources/orbital-strike-cannon-plugin.124251/");
        sender.sendMessage("§bAuthor: §fCabbage");
        sender.sendMessage("§bProgrammers: §fCabbage");
    }


    private void showGameRules(CommandSender sender) {
        sender.sendMessage("§6=== Available Game Rules ===");
        sender.sendMessage("§bTntDamage §7(int) - TNT damage multiplier (default: 10)");
        sender.sendMessage("§bFlySpeed §7(int) (player) - Set player fly speed");
        sender.sendMessage("§bNoExplosionDamage §7(true/false) - Toggle explosion damage");
        sender.sendMessage("\n§7Usage: /cabbage gamerules <rule> <value> [player]");
    }

    private void handleGameRules(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /cabbage gamerules <rule> <value> [player]");
            return;
        }

        String rule = args[1].toLowerCase();
        String value = args[2].toLowerCase();

        switch (rule) {
            case "tntdamage":
                try {
                    int damage = Integer.parseInt(value);
                    plugin.getConfig().set("tnt-damage", damage);
                    plugin.saveConfig();
                    sender.sendMessage("§aTNT damage set to " + damage);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
                break;

            case "flyspeed":
                if (args.length < 4) {
                    sender.sendMessage("§cUsage: /cabbage gamerules flyspeed <value> <player>");
                    return;
                }
                Player target = plugin.getServer().getPlayer(args[3]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return;
                }
                try {
                    float speed = Float.parseFloat(value) / 10f; // Convert to Minecraft's 0-1 range
                    speed = Math.max(0.1f, Math.min(1.0f, speed)); // Clamp between 0.1 and 1
                    target.setFlySpeed(speed);
                    sender.sendMessage("§aSet fly speed of " + target.getName() + " to " + (speed * 10));
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
                break;

            case "noexplosiondamage":
                boolean noExplosionDamage = Boolean.parseBoolean(value);
                plugin.getConfig().set("no-explosion-damage", noExplosionDamage);
                plugin.saveConfig();
                sender.sendMessage("§aNo explosion damage set to " + noExplosionDamage);
                break;

            default:
                sender.sendMessage("§cUnknown game rule!");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterCompletions(Arrays.asList("credits", "gamerules"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("gamerules")) {
            return filterCompletions(Arrays.asList("tntdamage", "flyspeed", "noexplosiondamage"), args[1]);
        }
        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("noexplosiondamage")) {
                return filterCompletions(Arrays.asList("true", "false"), args[2]);
            }
        }
        return new ArrayList<>();
    }

    private List<String> filterCompletions(List<String> completions, String partial) {
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(partial.toLowerCase())) {
                filtered.add(completion);
            }
        }
        return filtered;
    }
}