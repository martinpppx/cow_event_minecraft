package com.event.cowevent;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class CowEvent extends JavaPlugin implements Listener {

    private boolean eventActive = false; // Flag to indicate if the event is active
    private Cow specialCow; // Reference to the special cow spawned during the event
    private BukkitRunnable eventTask; // Task that handles the event timing

    public void onEnable() {
        saveDefaultConfig(); 
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("cowevent").setExecutor(new CowEventCommand(this));

        // If auto event is enabled in the config, start the event task
        if (getConfig().getBoolean("auto_event_enabled")) {
            startEventTask();
        }
    }

    public void onDisable() {
        stopEventTask(); // Stop the event task
    }

    public void loadConfig() {
        reloadConfig(); 
        FileConfiguration config = getConfig(); 
        config.addDefault("auto_event_enabled", true);
        config.addDefault("event_interval", 1800);
        config.addDefault("spawn_x_min", -100);
        config.addDefault("spawn_x_max", 100);
        config.addDefault("spawn_z_min", -100);
        config.addDefault("spawn_z_max", 100);
        config.addDefault("reward_material", "DIAMOND");
        config.addDefault("reward_amount", 64);
        config.addDefault("cow_health", 100.0);
        config.addDefault("cow_speed", 1.0);
        config.addDefault("coordinate_approximation", 100);
        config.addDefault("spin_radius", 20);
        config.addDefault("spin_duration", 2);
        config.options().copyDefaults(true); 
        saveConfig(); 
    }

    // Method to start the special cow event
    public void startEvent() {
        if (!eventActive) { // If the event is not already active
            eventActive = true; // Set the event as active
            spawnSpecialCow(); // Spawn the special cow
        }
    }

    // Method to stop the special cow event
    public void stopEvent() {
        if (eventActive) { // If the event is active
            eventActive = false; // Set the event as inactive
            if (specialCow != null && !specialCow.isDead()) {
                specialCow.remove(); // Remove the special cow if it's still alive
            }
            Bukkit.broadcastMessage("§6[CowEvent] §cThe event has been stopped.");
        }
    }

    // Method to spawn the special cow
    private void spawnSpecialCow() {
        Random random = new Random(); 
        // Calculate random coordinates for the cow spawn within the defined range
        int x = random.nextInt(getConfig().getInt("spawn_x_max") - getConfig().getInt("spawn_x_min") + 1) + getConfig().getInt("spawn_x_min");
        int z = random.nextInt(getConfig().getInt("spawn_z_max") - getConfig().getInt("spawn_z_min") + 1) + getConfig().getInt("spawn_z_min");
        // Get the highest block at the random coordinates and set the spawn location
        Location spawnLocation = new Location(Bukkit.getWorlds().get(0), x, Bukkit.getWorlds().get(0).getHighestBlockYAt(x, z) + 1, z);

        // Spawn the special cow at the location
        specialCow = (Cow) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.COW);
        // Set custom name and properties for the cow
        specialCow.setCustomName("§c§lM§a§lu§b§lu§d§lu§e§lu§6§lu");
        specialCow.setCustomNameVisible(true);
        specialCow.setMaxHealth(getConfig().getDouble("cow_health"));
        specialCow.setHealth(getConfig().getDouble("cow_health"));
        specialCow.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(getConfig().getDouble("cow_speed"));

        // Approximate coordinates for the cow spawn message
        int approx = getConfig().getInt("coordinate_approximation");
        // Broadcast a message with the approximate coordinates of the special cow
        Bukkit.broadcastMessage(String.format("§6[CowEvent] §eThe special cow has spawned between coordinates X %d-%d, Z %d-%d",
                x + approx, x - approx, z + approx, z - approx));
    }

    @EventHandler
    public void onCowKill(EntityDeathEvent event) {
        if (eventActive && event.getEntity() == specialCow) { // If the event is active and the entity is the special cow
            Player killer = event.getEntity().getKiller(); // Get the player who killed the cow
            if (killer != null) { // If there is a killer
                // Get the reward material and amount from the config
                Material rewardMaterial = Material.valueOf(getConfig().getString("reward_material"));
                int rewardAmount = getConfig().getInt("reward_amount");
                // Add the reward to the killer's inventory
                killer.getInventory().addItem(new ItemStack(rewardMaterial, rewardAmount));
                Bukkit.broadcastMessage("§6[CowEvent] §a" + killer.getName() + " has found and killed the special cow! They receive a reward!");
                stopEvent(); // Stop the event
                startEventTask(); // Start a new event task
            }
        }
    }

    @EventHandler
    public void onCowDamage(EntityDamageByEntityEvent event) {
        if (eventActive && event.getEntity() == specialCow) { // If the event is active and the entity is the special cow
            int spinRadius = getConfig().getInt("spin_radius"); // Get the spin radius from the config
            int spinDuration = getConfig().getInt("spin_duration"); // Get the spin duration from the config
            
            // For each player in the world
            for (Player player : event.getEntity().getWorld().getPlayers()) {
                // If the player is within the spin radius of the cow
                if (player.getLocation().distance(event.getEntity().getLocation()) <= spinRadius) {
                    spinPlayer(player, spinDuration); // Spin the player
                }
            }
        }
    }

    // Method to spin a player for a certain duration
    private void spinPlayer(Player player, int duration) {
        new BukkitRunnable() {
            int ticks = 0; // Counter for the number of ticks
            final int maxTicks = duration * 20; // Convert duration to ticks (20 ticks per second)

            @Override
            public void run() {
                if (ticks >= maxTicks) { // If the duration has passed
                    this.cancel(); // Cancel the task
                    return;
                }

                // Get the player's location and direction
                Location loc = player.getLocation();
                Vector direction = loc.getDirection().setY(0).normalize();
                // Calculate the new direction for spinning
                double x = direction.getZ();
                double z = -direction.getX();

                // Increase the yaw to spin the player
                loc.setYaw(loc.getYaw() + 18); // 360 degrees / 20 ticks = 18 degrees per tick
                player.teleport(loc); // Teleport the player to the new location

                ticks++; // Increment the tick counter
            }
        }.runTaskTimer(this, 0L, 1L); // Schedule the task to run every tick
    }

    // Method to start the event task
    private void startEventTask() {
        stopEventTask(); // Stop any existing event task
        eventTask = new BukkitRunnable() {
            @Override
            public void run() {
                startEvent(); // Start the event
            }
        };
        // Schedule the event task to run at the interval specified in the config
        eventTask.runTaskTimer(this, 0L, getConfig().getLong("event_interval") * 20L);
    }

    // Method to stop the event task
    private void stopEventTask() {
        if (eventTask != null) { // If there is an event task
            eventTask.cancel(); // Cancel the task
            eventTask = null; // Set the reference to null
        }
    }
}
