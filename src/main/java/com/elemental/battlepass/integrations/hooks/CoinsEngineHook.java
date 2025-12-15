// ============================================================================
// FILE: CoinsEngineHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CoinsEngineHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public CoinsEngineHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> receiveEventClass = Class.forName("su.nightexpress.coinsengine.api.event.CoinsReceiveEvent");
            Class<?> transferEventClass = Class.forName("su.nightexpress.coinsengine.api.event.CoinsTransferEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                receiveEventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onCoinsReceive(event),
                plugin
            );
            
            plugin.getServer().getPluginManager().registerEvent(
                transferEventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onCoinsTransfer(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("CoinsEngine not found, skipping integration");
        }
    }

    public void onCoinsReceive(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
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

    public void onCoinsTransfer(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
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