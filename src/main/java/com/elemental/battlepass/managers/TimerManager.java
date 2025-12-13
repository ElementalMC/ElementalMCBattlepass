// ============================================================================
// FILE: TimerManager.java
// LOCATION: src/main/java/com/elemental/battlepass/managers/
// ============================================================================
package com.elemental.battlepass.managers;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimerManager {
    private final ElementalBattlepassTracker plugin;
    private final Map<UUID, Long> sessionStarts = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastDeathTime = new ConcurrentHashMap<>();
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> killStreaks = new ConcurrentHashMap<>();

    public TimerManager(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
        startPlaytimeTracker();
    }

    private void startPlaytimeTracker() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                Long sessionStart = sessionStarts.get(player.getUniqueId());
                if (sessionStart != null) {
                    plugin.getStatTracker().trackPlaytime(player, 1);
                    plugin.getQuestManager().incrementProgress(player.getUniqueId(), "PLAYTIME_TOTAL", null, 1);
                }
            }
        }, 20L, 20L);
    }

    public void startSession(UUID uuid) {
        sessionStarts.put(uuid, System.currentTimeMillis());
    }

    public void endSession(UUID uuid) {
        Long start = sessionStarts.remove(uuid);
        if (start != null) {
            long duration = (System.currentTimeMillis() - start) / 1000;
            plugin.getStatTracker().incrementStat(uuid, "SESSION_TIME", null, duration);
        }
    }

    public long getSessionTime(UUID uuid) {
        Long start = sessionStarts.get(uuid);
        return start != null ? (System.currentTimeMillis() - start) / 1000 : 0;
    }

    public void recordDeath(UUID uuid) {
        lastDeathTime.put(uuid, System.currentTimeMillis());
        killStreaks.put(uuid, 0);
    }

    public long getTimeSinceLastDeath(UUID uuid) {
        Long lastDeath = lastDeathTime.get(uuid);
        return lastDeath != null ? (System.currentTimeMillis() - lastDeath) / 1000 : Long.MAX_VALUE;
    }

    public void incrementKillStreak(UUID uuid) {
        killStreaks.merge(uuid, 1, Integer::sum);
    }

    public int getKillStreak(UUID uuid) {
        return killStreaks.getOrDefault(uuid, 0);
    }

    public boolean checkCooldown(String key, long cooldownSeconds) {
        Long lastUse = cooldowns.get(key);
        long currentTime = System.currentTimeMillis();
        
        if (lastUse == null || (currentTime - lastUse) / 1000 >= cooldownSeconds) {
            cooldowns.put(key, currentTime);
            return true;
        }
        return false;
    }

    public void shutdown() {
        for (UUID uuid : sessionStarts.keySet()) {
            endSession(uuid);
        }
    }
}