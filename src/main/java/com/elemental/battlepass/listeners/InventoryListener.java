// ============================================================================
// FILE: InventoryListener.java
// LOCATION: src/main/java/com/elemental/battlepass/listeners/
// ============================================================================
package com.elemental.battlepass.listeners;

import com.elemental.battlepass.ElementalBattlepassTracker;
import org.bukkit.event.Listener;

public class InventoryListener implements Listener {
    private final ElementalBattlepassTracker plugin;

    public InventoryListener(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }
}