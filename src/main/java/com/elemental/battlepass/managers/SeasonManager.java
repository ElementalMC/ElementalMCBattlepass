// ============================================================================
// FILE 3: SeasonManager.java
// LOCATION: src/main/java/com/elemental/battlepass/managers/SeasonManager.java
// REPLACE ENTIRE FILE
// ============================================================================
package com.elemental.battlepass.managers;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import com.elemental.battlepass.models.Season;

import java.sql.*;
import java.util.logging.Level;

public class SeasonManager {

    private final ElementalMCBattlepassTracker plugin;
    private Season activeSeason;

    public SeasonManager(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void loadActiveSeason() {
        if (!plugin.getDatabaseManager().isConnected()) {
            plugin.getLogger().warning("Cannot load season - database not connected!");
            return;
        }
        
        String sql = "SELECT * FROM seasons WHERE active = TRUE LIMIT 1";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                activeSeason = new Season(
                    rs.getInt("season_id"),
                    rs.getString("season_name"),
                    rs.getTimestamp("start_date").getTime(),
                    rs.getTimestamp("end_date") != null ? rs.getTimestamp("end_date").getTime() : null,
                    rs.getBoolean("active")
                );
                plugin.getLogger().info("Loaded active season: " + activeSeason.getSeasonName() + " (ID: " + activeSeason.getSeasonId() + ")");
            } else {
                plugin.getLogger().warning("No active season found! Create one with /battlepass season create <name>");
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load active season", e);
        }
    }

    public int createSeason(String name) {
        if (!plugin.getDatabaseManager().isConnected()) {
            plugin.getLogger().warning("Cannot create season - database not connected!");
            return -1;
        }
        
        String deactivateSql = "UPDATE seasons SET active = FALSE WHERE active = TRUE";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(deactivateSql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to deactivate seasons", e);
            return -1;
        }

        String sql = "INSERT INTO seasons (season_name, start_date, active) VALUES (?, NOW(), TRUE)";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int seasonId = rs.getInt(1);
                loadActiveSeason();
                plugin.getLogger().info("Created new season: " + name + " (ID: " + seasonId + ")");
                return seasonId;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create season", e);
        }
        
        return -1;
    }

    public boolean endSeason(int seasonId) {
        if (!plugin.getDatabaseManager().isConnected()) {
            plugin.getLogger().warning("Cannot end season - database not connected!");
            return false;
        }
        
        String sql = "UPDATE seasons SET active = FALSE, end_date = NOW() WHERE season_id = ?";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, seasonId);
            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                plugin.getLogger().info("Ended season ID: " + seasonId);
                activeSeason = null;
                return true;
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to end season", e);
        }
        
        return false;
    }

    public String getPlayerTier(String uuid, int seasonId) {
        if (!plugin.getDatabaseManager().isConnected()) {
            return "free";
        }
        
        String sql = "SELECT tier FROM player_tiers WHERE uuid = ? AND season_id = ?";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setInt(2, seasonId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("tier");
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get player tier", e);
        }
        
        return "free";
    }

    public void setPlayerTier(String uuid, int seasonId, String tier) {
        String sql = "INSERT INTO player_tiers (uuid, season_id, tier) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE tier = VALUES(tier)";
        
        plugin.getDatabaseManager().executeAsync(sql, uuid, seasonId, tier);
    }

    public Season getActiveSeason() {
        return activeSeason;
    }

    public int getActiveSeasonId() {
        return activeSeason != null ? activeSeason.getSeasonId() : -1;
    }
}