// ============================================================================
// FILE: ZAuctionHouseHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionSellEvent;
import fr.maxlego08.zauctionhouse.api.event.events.AuctionBuyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ZAuctionHouseHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public ZAuctionHouseHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onAuctionSell(AuctionSellEvent event) {
        Player player = event.getPlayer();
        
        plugin.getStatTracker().trackAuctionSold(player);
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_SELL", null, 1);
    }

    @EventHandler
    public void onAuctionBuy(AuctionBuyEvent event) {
        Player player = event.getPlayer();
        
        plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_BUY", null, 1);
    }
}