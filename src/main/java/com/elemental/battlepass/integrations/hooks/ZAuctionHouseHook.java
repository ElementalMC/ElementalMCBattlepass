// ============================================================================
// FILE: ZAuctionHouseHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class ZAuctionHouseHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public ZAuctionHouseHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> sellEventClass = Class.forName("fr.maxlego08.zauctionhouse.api.event.events.AuctionSellEvent");
            Class<?> buyEventClass = Class.forName("fr.maxlego08.zauctionhouse.api.event.events.AuctionBuyEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                sellEventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onAuctionSell(event),
                plugin
            );
            
            plugin.getServer().getPluginManager().registerEvent(
                buyEventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onAuctionBuy(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("zAuctionHouse not found, skipping integration");
        }
    }

    public void onAuctionSell(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getStatTracker().trackAuctionSold(player);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_SELL", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }

    public void onAuctionBuy(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "AUCTION_BUY", null, 1);
        } catch (Exception e) {
            // Silently fail
        }
    }
}