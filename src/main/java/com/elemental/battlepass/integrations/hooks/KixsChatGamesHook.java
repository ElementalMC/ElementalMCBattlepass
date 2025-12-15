// ============================================================================
// FILE: KixsChatGamesHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class KixsChatGamesHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public KixsChatGamesHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> eventClass = Class.forName("io.github.kixsdesigns.chatgames.events.ChatGameWinEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onChatGameWin(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("KixsChatGames not found, skipping integration");
        }
    }

    public void onChatGameWin(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "WIN_CHAT_GAME", null, 1);
            plugin.getStatTracker().incrementStat(player.getUniqueId(), "CHAT_GAME_WIN", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }
}
