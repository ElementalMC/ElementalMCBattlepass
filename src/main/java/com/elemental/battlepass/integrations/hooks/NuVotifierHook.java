// ============================================================================
// FILE: NuVotifierHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
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
        try {
            Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("NuVotifier classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onVote(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getVote = eventClass.getMethod("getVote");
            Object vote = getVote.invoke(event);
            
            java.lang.reflect.Method getUsername = vote.getClass().getMethod("getUsername");
            String username = (String) getUsername.invoke(vote);
            
            Player player = Bukkit.getPlayer(username);
            if (player != null) {
                plugin.getStatTracker().trackVote(player);
                plugin.getQuestManager().incrementProgress(player.getUniqueId(), "VOTE", null, 1);
            }
        } catch (Exception e) {
            // Silently fail
        }
    }
}
