// ============================================================================
// FILE: CombatListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatListener implements Listener {
    private final ElementalBattlepassTracker plugin;

    public CombatListener(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer != null && !(entity instanceof Player)) {
            String mobType = entity.getType().name();
            
            plugin.getStatTracker().trackMobKill(killer, mobType);
            plugin.getQuestManager().incrementProgress(killer.getUniqueId(), "KILL_MOB", mobType, 1);
            plugin.getQuestManager().incrementProgress(killer.getUniqueId(), "KILL_MOB", null, 1);
            
            plugin.getTimerManager().incrementKillStreak(killer.getUniqueId());
            int streak = plugin.getTimerManager().getKillStreak(killer.getUniqueId());
            plugin.getQuestManager().incrementProgress(killer.getUniqueId(), "KILL_STREAK", String.valueOf(streak), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getTimerManager().recordDeath(player.getUniqueId());

        Player killer = player.getKiller();
        if (killer != null) {
            plugin.getStatTracker().trackPlayerKill(killer);
            plugin.getQuestManager().incrementProgress(killer.getUniqueId(), "KILL_PLAYER", null, 1);
            
            plugin.getTimerManager().incrementKillStreak(killer.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            double damage = event.getFinalDamage();
            
            plugin.getStatTracker().trackDamageDealt(player, damage);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "DEAL_DAMAGE", null, (int) damage);
        }
    }
}
