// ============================================================================
// FILE: NuVotifierHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NuVotifierHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public NuVotifierHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        String username = event.getVote().getUsername();
        Player player = Bukkit.getPlayer(username);
        
        if (player != null) {
            plugin.getStatTracker().trackVote(player);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "VOTE", null, 1);
        }
    }
}