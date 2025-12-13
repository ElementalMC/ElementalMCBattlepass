// ============================================================================
// FILE: MovementListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementListener implements Listener {
    private final ElementalBattlepassTracker plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private final Map<UUID, Long> lastCheck = new HashMap<>();
    private final Map<UUID, String> lastBiome = new HashMap<>();

    public MovementListener(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        Long lastCheckTime = lastCheck.get(uuid);
        int throttleTicks = plugin.getConfigManager().getMovementThrottleTicks();
        if (lastCheckTime != null && currentTime - lastCheckTime < throttleTicks * 50) {
            return;
        }
        lastCheck.put(uuid, currentTime);

        Location to = event.getTo();
        Location from = lastLocations.get(uuid);
        
        if (from != null && to != null && !from.getWorld().equals(to.getWorld())) {
            from = null;
        }

        if (from != null && to != null) {
            double distance = from.distance(to);
            if (distance > 0.1) {
                String moveType = player.isFlying() ? "FLY" : 
                                player.isInWater() ? "SWIM" : 
                                player.isSprinting() ? "SPRINT" : "WALK";
                
                plugin.getStatTracker().trackDistance(player, moveType, distance);
                plugin.getQuestManager().incrementProgress(uuid, "TRAVEL_DISTANCE", null, (int) distance);
            }
        }

        String currentBiome = to.getBlock().getBiome().name();
        String previousBiome = lastBiome.get(uuid);
        
        if (!currentBiome.equals(previousBiome)) {
            plugin.getStatTracker().trackBiomeVisit(player, currentBiome);
            plugin.getQuestManager().incrementProgress(uuid, "VISIT_BIOME", currentBiome, 1);
            lastBiome.put(uuid, currentBiome);
        }

        lastLocations.put(uuid, to);
    }
}
