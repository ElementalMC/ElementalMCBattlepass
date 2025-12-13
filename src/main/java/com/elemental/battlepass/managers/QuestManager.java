// ============================================================================
// FILE: QuestManager.java
// LOCATION: src/main/java/com/elemental/battlepass/managers/
// ============================================================================
package com.elemental.battlepass.managers;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.elemental.battlepass.models.Quest;
import com.elemental.battlepass.models.QuestProgress;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class QuestManager {

    private final ElementalBattlepassTracker plugin;
    private final Map<String, Quest> quests = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, QuestProgress>> playerProgress = new ConcurrentHashMap<>();
    
    public QuestManager(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void loadQuestsForActiveSeason() {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) {
            plugin.getLogger().warning("No active season found, cannot load quests!");
            return;
        }

        String sql = "SELECT * FROM quests WHERE season_id = ?";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, seasonId);
            ResultSet rs = stmt.executeQuery();
            
            quests.clear();
            while (rs.next()) {
                Quest quest = new Quest(
                    rs.getString("quest_id"),
                    rs.getInt("season_id"),
                    rs.getString("quest_type"),
                    rs.getString("target"),
                    rs.getInt("required_amount"),
                    rs.getInt("time_limit_seconds"),
                    rs.getBoolean("reset_on_fail"),
                    rs.getString("display_name"),
                    rs.getString("description")
                );
                quests.put(quest.getQuestId(), quest);
            }
            
            plugin.getLogger().info("Loaded " + quests.size() + " quests for season " + seasonId);
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load quests", e);
        }
    }

    public void loadPlayerProgress(UUID uuid) {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) return;

        String sql = "SELECT * FROM player_progress WHERE uuid = ? AND season_id = ?";
        
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, seasonId);
            ResultSet rs = stmt.executeQuery();
            
            Map<String, QuestProgress> progress = new ConcurrentHashMap<>();
            while (rs.next()) {
                QuestProgress qp = new QuestProgress(
                    rs.getString("uuid"),
                    rs.getInt("season_id"),
                    rs.getString("quest_id"),
                    rs.getInt("progress"),
                    rs.getLong("start_timestamp"),
                    rs.getBoolean("completed")
                );
                progress.put(qp.getQuestId(), qp);
            }
            
            playerProgress.put(uuid, progress);
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player progress for " + uuid, e);
        }
    }

    public void incrementProgress(UUID uuid, String questType, String target, int amount) {
        int seasonId = plugin.getSeasonManager().getActiveSeasonId();
        if (seasonId == -1) return;

        for (Quest quest : quests.values()) {
            if (!quest.getQuestType().equalsIgnoreCase(questType)) continue;
            if (quest.getTarget() != null && !quest.getTarget().isEmpty() && 
                !quest.getTarget().equalsIgnoreCase(target)) continue;

            incrementQuestProgress(uuid, quest, amount);
        }
    }

    private void incrementQuestProgress(UUID uuid, Quest quest, int amount) {
        Map<String, QuestProgress> progress = playerProgress.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
        QuestProgress qp = progress.get(quest.getQuestId());

        if (qp == null) {
            qp = new QuestProgress(
                uuid.toString(),
                quest.getSeasonId(),
                quest.getQuestId(),
                0,
                System.currentTimeMillis(),
                false
            );
            progress.put(quest.getQuestId(), qp);
        }

        if (qp.isCompleted()) return;

        if (quest.getTimeLimitSeconds() > 0) {
            long elapsed = (System.currentTimeMillis() - qp.getStartTimestamp()) / 1000;
            if (elapsed > quest.getTimeLimitSeconds()) {
                if (quest.isResetOnFail()) {
                    resetQuestProgress(uuid, quest.getQuestId());
                }
                return;
            }
        }

        qp.setProgress(qp.getProgress() + amount);
        
        if (qp.getProgress() >= quest.getRequiredAmount()) {
            qp.setCompleted(true);
            qp.setProgress(quest.getRequiredAmount());
            plugin.getLogger().info("Player " + uuid + " completed quest: " + quest.getQuestId());
        }

        saveProgressAsync(qp);
    }

    public void resetQuestProgress(UUID uuid, String questId) {
        Map<String, QuestProgress> progress = playerProgress.get(uuid);
        if (progress != null) {
            QuestProgress qp = progress.get(questId);
            if (qp != null) {
                qp.setProgress(0);
                qp.setStartTimestamp(System.currentTimeMillis());
                qp.setCompleted(false);
                saveProgressAsync(qp);
            }
        }
    }

    private CompletableFuture<Void> saveProgressAsync(QuestProgress qp) {
        String sql = "INSERT INTO player_progress (uuid, season_id, quest_id, progress, start_timestamp, completed, completed_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE progress = VALUES(progress), start_timestamp = VALUES(start_timestamp), " +
                     "completed = VALUES(completed), completed_at = VALUES(completed_at)";

        return plugin.getDatabaseManager().executeAsync(
            sql,
            qp.getUuid(),
            qp.getSeasonId(),
            qp.getQuestId(),
            qp.getProgress(),
            qp.getStartTimestamp(),
            qp.isCompleted(),
            qp.isCompleted() ? new java.sql.Timestamp(System.currentTimeMillis()) : null
        );
    }


    public void shutdown() {
        plugin.getLogger().info("Flushing all quest progress to database...");
        
        for (Map<String, QuestProgress> progressMap : playerProgress.values()) {
            for (QuestProgress qp : progressMap.values()) {
                saveProgressAsync(qp).join();
            }
        }
        
        playerProgress.clear();
        plugin.getLogger().info("Quest progress saved successfully!");
    }

    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    public Collection<Quest> getAllQuests() {
        return quests.values();
    }

    public QuestProgress getPlayerProgress(UUID uuid, String questId) {
        Map<String, QuestProgress> progress = playerProgress.get(uuid);
        return progress != null ? progress.get(questId) : null;
    }

    public Map<String, QuestProgress> getPlayerProgress(UUID uuid) {
        return playerProgress.getOrDefault(uuid, new HashMap<>());
    }
}