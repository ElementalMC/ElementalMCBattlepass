// ============================================================================
// FILE: ZAuctionHouseHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ZAuctionHouseHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public ZAuctionHouseHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class.forName("fr.maxlego08.zauctionhouse.api.event.events.AuctionSellEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("zAuctionHouse classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onAuctionSell(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = Class.forName("fr.maxlego08.zauctionhouse.api.event.events.AuctionSellEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getStatTracker().trackAuctionSold(player);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_SELL", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }

    @EventHandler
    public void onAuctionBuy(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = Class.forName("fr.maxlego08.zauctionhouse.api.event.events.AuctionBuyEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_BUY", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }
}