// ============================================================================
// FILE: BountyHuntersHook.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.event.Listener;

public class BountyHuntersHook implements Listener {
    private final ElementalBattlepassTracker plugin;

    public BountyHuntersHook(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}