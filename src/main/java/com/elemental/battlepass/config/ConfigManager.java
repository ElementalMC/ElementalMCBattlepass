// ============================================================================
// FILE: ConfigManager.java
// PATH: src/main/java/com/elemental/battlepass/config/ConfigManager.java
// ============================================================================
package com.elemental.battlepass.config;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final ElementalMCBattlepassTracker plugin;
    private final FileConfiguration config;

    public ConfigManager(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public int getMovementThrottleTicks() {
        return config.getInt("performance.movement-throttle-ticks", 20);
    }

    public int getChatRateLimitSeconds() {
        return config.getInt("performance.chat-rate-limit-seconds", 2);
    }

    public boolean isDebugMode() {
        return config.getBoolean("debug", false);
    }
}