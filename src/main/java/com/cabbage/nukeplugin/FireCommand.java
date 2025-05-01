package com.cabbage.nukeplugin;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.util.Vector;

import java.util.Random;

public class FireCommand implements CommandExecutor {
    private final NukePlugin plugin;
    private final Random random = new Random();

    public FireCommand(NukePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 8 || args.length > 10) {
            sender.sendMessage("Usage: /fire <x> <y> <z> <payload> <fuse> <spacing> <use_motion> <motion_y> [rings] [tnt-per-ring]");
            return true;
        }

        try {
            // Parse coordinates, handling relative coordinates (~)
            double x, y, z;
            Location senderLoc;

            if (sender instanceof Player) {
                senderLoc = ((Player) sender).getLocation();
            } else if (sender instanceof BlockCommandSender) {
                senderLoc = ((BlockCommandSender) sender).getBlock().getLocation();
            } else {
                senderLoc = new Location(plugin.getServer().getWorlds().get(0), 0, 0, 0);
            }

            x = args[0].startsWith("~") ?
                    senderLoc.getX() + (args[0].length() > 1 ? Double.parseDouble(args[0].substring(1)) : 0) :
                    Double.parseDouble(args[0]);

            y = args[1].startsWith("~") ?
                    senderLoc.getY() + (args[1].length() > 1 ? Double.parseDouble(args[1].substring(1)) : 0) :
                    Double.parseDouble(args[1]);

            z = args[2].startsWith("~") ?
                    senderLoc.getZ() + (args[2].length() > 1 ? Double.parseDouble(args[2].substring(1)) : 0) :
                    Double.parseDouble(args[2]);

            String payload = args[3].toLowerCase();
            int fuseTicks = Integer.parseInt(args[4]);
            double spacing = Double.parseDouble(args[5]);
            boolean useMotion = args[6].equalsIgnoreCase("on");
            double motionY = Double.parseDouble(args[7]);

            // Optional parameters
            int rings = args.length > 8 ? Integer.parseInt(args[8]) : 10;
            int tntPerRing = args.length > 9 ? Integer.parseInt(args[9]) : 150;

            // Validate parameters
            if (fuseTicks < 0) {
                sender.sendMessage("Fuse ticks must be 0 or greater!");
                return true;
            }
            if (spacing <= 0) {
                sender.sendMessage("Spacing must be greater than 0!");
                return true;
            }
            if (rings < 1 || rings > 50) {
                sender.sendMessage("Rings must be between 1 and 50!");
                return true;
            }
            if (tntPerRing < 1 || tntPerRing > 500) {
                sender.sendMessage("TNT per ring must be between 1 and 500!");
                return true;
            }

            World world = senderLoc.getWorld();
            Location targetLocation = new Location(world, x, y, z);

            switch (payload) {
                case "nuke":
                    launchNuke(world, targetLocation, rings, tntPerRing, fuseTicks, spacing, useMotion, motionY);
                    sender.sendMessage(String.format("Launching nuke at coordinates: %.1f, %.1f, %.1f with %d rings, %d TNT per ring, %d tick fuse, %.1f spacing, motion %s, motion Y: %.1f",
                            x, y, z, rings, tntPerRing, fuseTicks, spacing, useMotion ? "on" : "off", motionY));
                    break;
                case "mushroom":
                    launchMushroomCloud(world, targetLocation, fuseTicks, spacing, useMotion, motionY);
                    sender.sendMessage(String.format("Launching mushroom cloud at coordinates: %.1f, %.1f, %.1f with %d tick fuse, %.1f spacing, motion %s, motion Y: %.1f",
                            x, y, z, fuseTicks, spacing, useMotion ? "on" : "off", motionY));
                    break;
                case "stab":
                    launchStab(world, x, z, fuseTicks, spacing, useMotion, motionY);
                    sender.sendMessage(String.format("Launching stab attack at coordinates: %.1f, %.1f with %d tick fuse, %.1f spacing, motion %s, motion Y: %.1f",
                            x, z, fuseTicks, spacing, useMotion ? "on" : "off", motionY));
                    break;
                default:
                    sender.sendMessage("Invalid payload type! Use 'nuke', 'mushroom', or 'stab'");
                    return true;
            }
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("Invalid number format! Please use valid numbers.");
            return true;
        }
    }

    private void launchNuke(World world, Location center, int rings, int tntPerRing, int fuseTicks, double spacing, boolean useMotion, double motionY) {
        int verticalLayers = 5; // Number of vertical layers
        double verticalSpacing = 5.0; // Spacing between vertical layers

        for (int layer = 0; layer < verticalLayers; layer++) {
            Location layerCenter = center.clone().add(0, layer * verticalSpacing, 0);

            for (int ring = 1; ring <= rings; ring++) {
                double radius = ring * spacing;
                double angleStep = 360.0 / tntPerRing;

                for (double angle = 0; angle < 360; angle += angleStep) {
                    Location spawnLoc;
                    if (useMotion) {
                        spawnLoc = layerCenter.clone();
                    } else {
                        double radian = Math.toRadians(angle);
                        double x = layerCenter.getX() + radius * Math.cos(radian);
                        double z = layerCenter.getZ() + radius * Math.sin(radian);
                        spawnLoc = new Location(world, x, layerCenter.getY(), z);
                    }

                    TNTPrimed tnt = (TNTPrimed) world.spawnEntity(spawnLoc, EntityType.TNT);
                    tnt.setFuseTicks(fuseTicks + (layer * 5)); // Stagger the explosions

                    if (useMotion) {
                        double radian = Math.toRadians(angle);
                        double velocityX = Math.cos(radian) * (radius * 0.2);
                        double velocityZ = Math.sin(radian) * (radius * 0.2);
                        double layerMotionY = motionY + (layer * 0.5); // Increase upward motion for each layer
                        Vector vector = new Vector(velocityX, layerMotionY, velocityZ);
                        tnt.setVelocity(vector);
                    }
                }
            }
        }
    }

    private void launchMushroomCloud(World world, Location center, int fuseTicks, double spacing, boolean useMotion, double motionY) {
        // Create the stem with vertical separation
        int stemLayers = 60;
        int tntPerStemLayer = 16;
        double stemVerticalSpacing = 2.0;

        // Create the cap with vertical layers
        int capLayers = 8;
        int tntPerCapRing = 300;
        double maxCapRadius = 80.0;
        double capVerticalSpacing = 4.0;

        if (useMotion) {
            // Spawn stem TNT with vertical separation
            for (int layer = 0; layer < stemLayers; layer++) {
                Location layerCenter = center.clone().add(0, layer * stemVerticalSpacing, 0);

                for (int i = 0; i < tntPerStemLayer; i++) {
                    TNTPrimed tnt = (TNTPrimed) world.spawnEntity(layerCenter, EntityType.TNT);
                    tnt.setFuseTicks(fuseTicks + (layer * 2)); // Stagger explosions

                    double angle = (360.0 / tntPerStemLayer) * i;
                    double radian = Math.toRadians(angle);
                    double spreadFactor = 0.1;
                    double heightBoost = 3.0 + (layer * 0.1);
                    Vector vector = new Vector(
                            Math.cos(radian) * spreadFactor,
                            motionY + heightBoost,
                            Math.sin(radian) * spreadFactor
                    );
                    tnt.setVelocity(vector);
                }
            }

            // Spawn cap TNT with vertical layers
            for (int layer = 0; layer < capLayers; layer++) {
                Location layerCenter = center.clone().add(0, stemLayers * stemVerticalSpacing + (layer * capVerticalSpacing), 0);
                double layerRadius = maxCapRadius * ((double)(layer + 1) / capLayers);
                double angleStep = 360.0 / tntPerCapRing;

                for (double angle = 0; angle < 360; angle += angleStep) {
                    TNTPrimed tnt = (TNTPrimed) world.spawnEntity(layerCenter, EntityType.TNT);
                    tnt.setFuseTicks(fuseTicks + stemLayers * 2 + (layer * 4)); // Further stagger cap explosions

                    double radian = Math.toRadians(angle);
                    double outwardForce = layerRadius * 0.15;
                    Vector vector = new Vector(
                            Math.cos(radian) * outwardForce,
                            motionY + 2.5 - (layer * 0.2),
                            Math.sin(radian) * outwardForce
                    );
                    tnt.setVelocity(vector);
                }
            }
        } else {
            // Static placement with vertical separation
            double stemHeight = 80.0;
            double stemRadius = 8.0;

            for (double y = 0; y < stemHeight; y += spacing) {
                for (double angle = 0; angle < 360; angle += (360.0 / tntPerStemLayer)) {
                    double radian = Math.toRadians(angle);
                    double x = center.getX() + (stemRadius * Math.cos(radian));
                    double z = center.getZ() + (stemRadius * Math.sin(radian));
                    Location spawnLoc = new Location(world, x, center.getY() + y, z);

                    TNTPrimed tnt = (TNTPrimed) world.spawnEntity(spawnLoc, EntityType.TNT);
                    tnt.setFuseTicks(fuseTicks + (int)(y * 0.5)); // Stagger explosions based on height
                }
            }

            Location capCenter = center.clone().add(0, stemHeight, 0);
            for (int layer = 0; layer < capLayers; layer++) {
                double layerRadius = maxCapRadius * ((double)(layer + 1) / capLayers);
                double angleStep = 360.0 / tntPerCapRing;
                double layerHeight = capCenter.getY() - (layer * 2);

                for (double angle = 0; angle < 360; angle += angleStep) {
                    double radian = Math.toRadians(angle);
                    double x = capCenter.getX() + (layerRadius * Math.cos(radian));
                    double z = capCenter.getZ() + (layerRadius * Math.sin(radian));

                    Location spawnLoc = new Location(world, x, layerHeight, z);
                    TNTPrimed tnt = (TNTPrimed) world.spawnEntity(spawnLoc, EntityType.TNT);
                    tnt.setFuseTicks(fuseTicks + (int)(stemHeight * 0.5) + (layer * 4));
                }
            }
        }
    }

    private void launchStab(World world, double x, double z, int fuseTicks, double spacing, boolean useMotion, double motionY) {
        Location spawnLoc = new Location(world, x, world.getMaxHeight(), z);

        for (double y = world.getMaxHeight(); y > world.getMinHeight(); y -= spacing) {
            if (useMotion) {
                TNTPrimed tnt = (TNTPrimed) world.spawnEntity(spawnLoc, EntityType.TNT);
                tnt.setFuseTicks(fuseTicks);

                double motionX = (random.nextDouble() - 0.5) * 0.2;
                double motionZ = (random.nextDouble() - 0.5) * 0.2;
                Vector vector = new Vector(motionX, motionY, motionZ);
                tnt.setVelocity(vector);
            } else {
                Location tntLoc = new Location(world, x, y, z);
                TNTPrimed tnt = (TNTPrimed) world.spawnEntity(tntLoc, EntityType.TNT);
                tnt.setFuseTicks(fuseTicks);
            }
        }
    }
}