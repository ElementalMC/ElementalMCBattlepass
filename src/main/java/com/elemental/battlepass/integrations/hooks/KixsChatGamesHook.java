// ============================================================================
// FILE: KixsChatGamesHook.java
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KixsChatGamesHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;
    private boolean registered = false;

    public KixsChatGamesHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class.forName("io.github.kixsdesigns.chatgames.events.ChatGameWinEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            registered = true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("KixsChatGames classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onChatGameWin(org.bukkit.event.Event event) {
        if (!registered) return;
        
        try {
            Class<?> eventClass = Class.forName("io.github.kixsdesigns.chatgames.events.ChatGameWinEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "WIN_CHAT_GAME", null, 1);
            plugin.getStatTracker().incrementStat(player.getUniqueId(), "CHAT_GAME_WIN", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }
}