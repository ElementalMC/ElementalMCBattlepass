// ============================================================================
// FILE: KixsChatGamesHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import io.github.kixsdesigns.chatgames.events.ChatGameWinEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KixsChatGamesHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public KixsChatGamesHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChatGameWin(ChatGameWinEvent event) {
        Player player = event.getPlayer();
        
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "WIN_CHAT_GAME", null, 1);
        plugin.getStatTracker().incrementStat(player.getUniqueId(), "CHAT_GAME_WIN", null, 1);
    }
}