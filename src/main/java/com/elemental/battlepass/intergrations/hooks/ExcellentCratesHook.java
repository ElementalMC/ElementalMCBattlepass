// ============================================================================
// FILE: ExcellentCratesHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import su.nightexpress.excellentcrates.api.event.CrateOpenEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExcellentCratesHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public ExcellentCratesHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCrateOpen(CrateOpenEvent event) {
        Player player = event.getPlayer();
        String crateId = event.getCrate().getId();
        
        plugin.getStatTracker().trackCrateOpen(player, crateId);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "OPEN_CRATE", crateId, 1);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "OPEN_CRATE", null, 1);
    }
}