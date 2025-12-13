// ============================================================================
// FILE: PlayerConnectionListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

public class PlayerConnectionListener implements Listener {
    private final ElementalBattlepassTracker plugin;

    public PlayerConnectionListener(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        registerPlayer(uuid, player.getName());
        
        plugin.getQuestManager().loadPlayerProgress(uuid);
        
        plugin.getTimerManager().startSession(uuid);
        
        trackLoginStreak(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        plugin.getTimerManager().endSession(uuid);
    }

    private void registerPlayer(UUID uuid, String username) {
        String sql = "INSERT INTO players (uuid, username) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE username = VALUES(username)";
        plugin.getDatabaseManager().executeAsync(sql, uuid.toString(), username);
    }

    private void trackLoginStreak(UUID uuid) {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String selectSql = "SELECT current_streak, last_login_date FROM player_login_streak " +
                                 "WHERE uuid = ? AND season_id = ?";
                PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                selectStmt.setString(1, uuid.toString());
                selectStmt.setInt(2, seasonId);
                
                ResultSet rs = selectStmt.executeQuery();
                LocalDate today = LocalDate.now();
                int newStreak = 1;
                
                if (rs.next()) {
                    LocalDate lastLogin = rs.getDate("last_login_date").toLocalDate();
                    int currentStreak = rs.getInt("current_streak");
                    
                    if (lastLogin.equals(today)) {
                        return;
                    } else if (lastLogin.plusDays(1).equals(today)) {
                        newStreak = currentStreak + 1;
                    }
                }
                
                String upsertSql = "INSERT INTO player_login_streak (uuid, season_id, current_streak, longest_streak, last_login_date) " +
                                 "VALUES (?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE " +
                                 "current_streak = VALUES(current_streak), " +
                                 "longest_streak = GREATEST(longest_streak, VALUES(longest_streak)), " +
                                 "last_login_date = VALUES(last_login_date)";
                
                PreparedStatement upsertStmt = conn.prepareStatement(upsertSql);
                upsertStmt.setString(1, uuid.toString());
                upsertStmt.setInt(2, seasonId);
                upsertStmt.setInt(3, newStreak);
                upsertStmt.setInt(4, newStreak);
                upsertStmt.setDate(5, java.sql.Date.valueOf(today));
                upsertStmt.executeUpdate();
                
                plugin.getQuestManager().incrementProgress(uuid, "LOGIN_STREAK", null, newStreak);
                
            } catch (SQLException e) {
                plugin.getLogger().warning("Failed to track login streak: " + e.getMessage());
            }
        });
    }
}