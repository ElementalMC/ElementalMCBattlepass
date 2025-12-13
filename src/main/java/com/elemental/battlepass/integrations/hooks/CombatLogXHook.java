// ============================================================================
// FILE: CombatLogXHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.github.sirblobman.combatlogx.api.event.PlayerTagEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatLogXHook implements Listener {
    private final ElementalBattlepassTracker plugin;
    private final Set<UUID> taggedPlayers = new HashSet<>();

    public CombatLogXHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerTag(PlayerTagEvent event) {
        Player player = event.getPlayer();
        taggedPlayers.add(player.getUniqueId());
    }

    public boolean isInCombat(UUID uuid) {
        return taggedPlayers.contains(uuid);
    }
}
