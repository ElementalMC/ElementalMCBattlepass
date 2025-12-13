// ============================================================================
// FILE: InventoryListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.event.Listener;

public class InventoryListener implements Listener {
    private final ElementalMCBattlepassTracker plugin;

    public InventoryListener(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
    }
}