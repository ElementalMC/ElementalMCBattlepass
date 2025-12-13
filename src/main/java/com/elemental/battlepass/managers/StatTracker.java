
// ============================================================================
// FILE: StatTracker.java
// LOCATION: src/main/java/com/elemental/battlepass/managers/
// ============================================================================
package com.elemental.battlepass.managers;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StatTracker {

    private final ElementalBattlepassTracker plugin;
    private final Map<String, Long> cachedStats = new ConcurrentHashMap<>();
    private static final int FLUSH_INTERVAL = 300;

    public StatTracker(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
        startAutoFlush();
    }

    private void startAutoFlush() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            flushAllStats();
        }, FLUSH_INTERVAL * 20L, FLUSH_INTERVAL * 20L);
    }

    public void incrementStat(UUID uuid, String statType, String statTarget, long amount) {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) return;

        String key = generateKey(uuid, seasonId, statType, statTarget);
        cachedStats.merge(key, amount, Long::sum);
    }

    public long getStat(UUID uuid, String statType, String statTarget) {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) return 0;

        String key = generateKey(uuid, seasonId, statType, statTarget);
        
        if (cachedStats.containsKey(key)) {
            return cachedStats.get(key);
        }

        String sql = "SELECT amount FROM player_stats WHERE uuid = ? AND season_id = ? AND stat_type = ? AND stat_target = ?";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, seasonId);
            stmt.setString(3, statType);
            stmt.setString(4, statTarget != null ? statTarget : "");
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long amount = rs.getLong("amount");
                cachedStats.put(key, amount);
                return amount;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load stat: " + key, e);
        }
        
        return 0;
    }

    private String generateKey(UUID uuid, int seasonId, String statType, String statTarget) {
        return uuid + ":" + seasonId + ":" + statType + ":" + (statTarget != null ? statTarget : "");
    }

    public void flushAllStats() {
        if (cachedStats.isEmpty()) return;

        plugin.getLogger().info("Flushing " + cachedStats.size() + " cached stats to database...");
        
        String sql = "INSERT INTO player_stats (uuid, season_id, stat_type, stat_target, amount) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)";

        for (Map.Entry<String, Long> entry : cachedStats.entrySet()) {
            String[] parts = entry.getKey().split(":", 4);
            String uuid = parts[0];
            int seasonId = Integer.parseInt(parts[1]);
            String statType = parts[2];
            String statTarget = parts[3];
            long amount = entry.getValue();

            plugin.getDatabaseManager().executeAsync(sql, uuid, seasonId, statType, statTarget, amount);
        }

        cachedStats.clear();
        plugin.getLogger().info("Stats flushed successfully!");
    }

    public void shutdown() {
        flushAllStats();
    }

    public void trackBlockBreak(Player player, String blockType) {
        incrementStat(player.getUniqueId(), "BLOCK_BREAK", blockType, 1);
        incrementStat(player.getUniqueId(), "BLOCK_BREAK", null, 1);
    }

    public void trackBlockPlace(Player player, String blockType) {
        incrementStat(player.getUniqueId(), "BLOCK_PLACE", blockType, 1);
        incrementStat(player.getUniqueId(), "BLOCK_PLACE", null, 1);
    }

    public void trackMobKill(Player player, String mobType) {
        incrementStat(player.getUniqueId(), "MOB_KILL", mobType, 1);
        incrementStat(player.getUniqueId(), "MOB_KILL", null, 1);
    }

    public void trackPlayerKill(Player player) {
        incrementStat(player.getUniqueId(), "PLAYER_KILL", null, 1);
    }

    public void trackDamageDealt(Player player, double damage) {
        incrementStat(player.getUniqueId(), "DAMAGE_DEALT", null, (long) damage);
    }

    public void trackChatMessage(Player player) {
        incrementStat(player.getUniqueId(), "CHAT_MESSAGE", null, 1);
    }

    public void trackDistance(Player player, String type, double distance) {
        incrementStat(player.getUniqueId(), "DISTANCE_" + type, null, (long) distance);
    }

    public void trackBiomeVisit(Player player, String biome) {
        incrementStat(player.getUniqueId(), "BIOME_VISIT", biome, 1);
    }

    public void trackBendingUse(Player player, String element, String ability) {
        incrementStat(player.getUniqueId(), "BENDING_USE", element, 1);
        incrementStat(player.getUniqueId(), "BENDING_ABILITY", ability, 1);
    }

    public void trackMoneyEarned(Player player, double amount) {
        incrementStat(player.getUniqueId(), "MONEY_EARNED", null, (long) amount);
    }

    public void trackMoneySpent(Player player, double amount) {
        incrementStat(player.getUniqueId(), "MONEY_SPENT", null, (long) amount);
    }

    public void trackAuctionSold(Player player) {
        incrementStat(player.getUniqueId(), "AUCTION_SOLD", null, 1);
    }

    public void trackCrateOpen(Player player, String crateType) {
        incrementStat(player.getUniqueId(), "CRATE_OPEN", crateType, 1);
        incrementStat(player.getUniqueId(), "CRATE_OPEN", null, 1);
    }

    public void trackVote(Player player) {
        incrementStat(player.getUniqueId(), "VOTE", null, 1);
    }

    public void trackBountyComplete(Player player) {
        incrementStat(player.getUniqueId(), "BOUNTY_COMPLETE", null, 1);
    }

    public void trackPlaytime(Player player, long seconds) {
        incrementStat(player.getUniqueId(), "PLAYTIME", null, seconds);
    }
}
