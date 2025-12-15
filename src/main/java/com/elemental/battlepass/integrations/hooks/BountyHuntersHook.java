// ============================================================================
// FILE: BountyHuntersHook.java
// PATH: src/main/java/com/elemental/battlepass/integrations/hooks/
// ============================================================================
package com.elemental.battlepass.integrations.hooks;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.event.Listener;

public class BountyHuntersHook implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public BountyHuntersHook(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        plugin.getLogger().info("BountyHunters hook registered (awaiting specific event implementation)");
    }
}