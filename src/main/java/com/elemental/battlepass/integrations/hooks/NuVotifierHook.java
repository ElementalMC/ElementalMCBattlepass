// ============================================================================
// FILE: NuVotifierHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class NuVotifierHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public NuVotifierHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> eventClass = Class.forName("com.vexsoftware.votifier.model.VotifierEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onVote(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("NuVotifier not found, skipping integration");
        }
    }

    public void onVote(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
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