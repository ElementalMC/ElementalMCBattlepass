// ============================================================================
// FILE: CoinsEngineHook.java
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CoinsEngineHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;
    private boolean registered = false;

    public CoinsEngineHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class.forName("su.nightexpress.coinsengine.api.event.CoinsReceiveEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            registered = true;
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("CoinsEngine classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onCoinsReceive(org.bukkit.event.Event event) {
        if (!registered) return;
        
        try {
            Class<?> eventClass = Class.forName("su.nightexpress.coinsengine.api.event.CoinsReceiveEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            
            java.lang.reflect.Method getAmount = eventClass.getMethod("getAmount");
            double amount = (Double) getAmount.invoke(event);
            
            plugin.getStatTracker().trackMoneyEarned(player, amount);
            plugin.getQuestManager().incrementProgress(player.getUniqueId(), "EARN_MONEY", null, (int) amount);
        } catch (Exception e) {
            // Silently fail
        }
    }

    @EventHandler
    public void onCoinsTransfer(org.bukkit.event.Event event) {
        if (!registered) return;
        
        try {
            Class<?> eventClass = Class.forName("su.nightexpress.coinsengine.api.event.CoinsTransferEvent");
            if (!eventClass.isInstance(event)) return;
            
            java.lang.reflect.Method getSender = eventClass.getMethod("getSender");
            Player sender = (Player) getSender.invoke(event);
            
            java.lang.reflect.Method getAmount = eventClass.getMethod("getAmount");
            double amount = (Double) getAmount.invoke(event);
            
            plugin.getStatTracker().trackMoneySpent(sender, amount);
            plugin.getQuestManager().incrementProgress(sender.getUniqueId(), "SPEND_MONEY", null, (int) amount);
        } catch (Exception e) {
            // Silently fail
        }
    }
}