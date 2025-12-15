// ============================================================================
// FILE: ExcellentCratesHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ExcellentCratesHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public ExcellentCratesHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> eventClass = Class.forName("su.nightexpress.excellentcrates.api.event.CrateOpenEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onCrateOpen(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("ExcellentCrates not found, skipping integration");
        }
    }

    public void onCrateOpen(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            java.lang.reflect.Method getCrate = eventClass.getMethod("getCrate");
            Object crate = getCrate.invoke(event);
            
            java.lang.reflect.Method getId = crate.getClass().getMethod("getId");
            String crateId = (String) getId.invoke(crate);
            
            plugin.getStatTracker().trackCrateOpen(player, crateId);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "OPEN_CRATE", crateId, 1);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "OPEN_CRATE", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }
}