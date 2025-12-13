// ============================================================================
// FILE: CoinsEngineHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import su.nightexpress.coinsengine.api.event.CoinsReceiveEvent;
import su.nightexpress.coinsengine.api.event.CoinsTransferEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoinsEngineHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public CoinsEngineHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onCoinsReceive(CoinsReceiveEvent event) {
        Player player = event.getPlayer();
        double amount = event.getAmount();
        
        plugin.getStatTracker().trackMoneyEarned(player, amount);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "EARN_MONEY", null, (int) amount);
    }

    @EventHandler
    public void onCoinsTransfer(CoinsTransferEvent event) {
        Player sender = event.getSender();
        double amount = event.getAmount();
        
        plugin.getStatTracker().trackMoneySpent(sender, amount);
        plugin.getQuestManager().incrementProgress(sender.getUniqueId(), "SPEND_MONEY", null, (int) amount);
    }
}
