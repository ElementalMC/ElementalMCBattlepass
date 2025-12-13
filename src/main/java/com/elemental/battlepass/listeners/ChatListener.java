// ============================================================================
// FILE: ChatListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public ChatListener(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        int rateLimitSeconds = plugin.getConfigManager().getChatRateLimitSeconds();
        if (!plugin.getTimerManager().checkCooldown("chat:" + uuid, rateLimitSeconds)) {
            return;
        }

        plugin.getStatTracker().trackChatMessage(player);
        plugin.getQuestManager().incrementProgress(uuid, "SEND_CHAT", null, 1);
    }
}