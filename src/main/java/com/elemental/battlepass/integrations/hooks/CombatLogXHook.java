// ============================================================================
// FILE: CombatLogXHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatLogXHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;
    private final Set<UUID> taggedPlayers = new HashSet<>();

    public CombatLogXHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            Class<?> eventClass = Class.forName("com.github.sirblobman.combatlogx.api.event.PlayerTagEvent");
            
            plugin.getServer().getPluginManager().registerEvent(
                eventClass,
                this,
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> onPlayerTag(event),
                plugin
            );
            
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("CombatLogX not found, skipping integration");
        }
    }

    public void onPlayerTag(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = event.getClass();
            
            java.lang.reflect.Method getPlayer = eventClass.getMethod("getPlayer");
            Player player = (Player) getPlayer.invoke(event);
            taggedPlayers.add(player.getUniqueId());
        } catch (Exception e) {
            // Silently fail
        }
    }

    public boolean isInCombat(UUID uuid) {
        return taggedPlayers.contains(uuid);
    }
}
