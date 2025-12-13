// ============================================================================
// FILE: CombatLogXHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
            Class.forName("com.github.sirblobman.combatlogx.api.event.PlayerTagEvent");
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("CombatLogX classes not found, skipping integration");
        }
    }

    @EventHandler
    public void onPlayerTag(org.bukkit.event.Event event) {
        try {
            Class<?> eventClass = Class.forName("com.github.sirblobman.combatlogx.api.event.PlayerTagEvent");
            if (!eventClass.isInstance(event)) return;
            
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
