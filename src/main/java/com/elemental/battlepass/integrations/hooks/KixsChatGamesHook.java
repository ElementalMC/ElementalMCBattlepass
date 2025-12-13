// ============================================================================
// FILE: KixsChatGamesHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KixsChatGamesHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public KixsChatGamesHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class.forName("io.github.kixsdesigns.chatgames.events.ChatGameWinEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("KixsChatGames classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onChatGameWin(org.bukkit.event.Event event) {
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