// ============================================================================
// FILE: IntegrationManager.java
// LOCATION: src/main/java/com/elemental/battlepass/integrations/
// ============================================================================
package com.elemental.battlepass.integrations;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.elemental.battlepass.integrations.hooks.*;
import org.bukkit.plugin.Plugin;

public class IntegrationManager {

    private final ElementalBattlepassTracker plugin;
    
    private ProjectKorraHook projectKorraHook;
    private CombatLogXHook combatLogXHook;
    private CoinsEngineHook coinsEngineHook;
    private ExcellentCratesHook excellentCratesHook;
    private KixsChatGamesHook kixsChatGamesHook;
    private NuVotifierHook nuVotifierHook;
    private ZAuctionHouseHook zAuctionHouseHook;
    private BountyHuntersHook bountyHuntersHook;

    public IntegrationManager(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getLogger().info("Initializing integrations...");

        if (isPluginEnabled("ProjectKorra")) {
            projectKorraHook = new ProjectKorraHook(plugin);
            projectKorraHook.hook();
            plugin.getLogger().info("✓ ProjectKorra integration enabled");
        }

        if (isPluginEnabled("CombatLogX")) {
            combatLogXHook = new CombatLogXHook(plugin);
            combatLogXHook.hook();
            plugin.getLogger().info("✓ CombatLogX integration enabled");
        }

        if (isPluginEnabled("CoinsEngine")) {
            coinsEngineHook = new CoinsEngineHook(plugin);
            coinsEngineHook.hook();
            plugin.getLogger().info("✓ CoinsEngine integration enabled");
        }

        if (isPluginEnabled("ExcellentCrates")) {
            excellentCratesHook = new ExcellentCratesHook(plugin);
            excellentCratesHook.hook();
            plugin.getLogger().info("✓ ExcellentCrates integration enabled");
        }

        if (isPluginEnabled("KixsChatGames")) {
            kixsChatGamesHook = new KixsChatGamesHook(plugin);
            kixsChatGamesHook.hook();
            plugin.getLogger().info("✓ KixsChatGames integration enabled");
        }

        if (isPluginEnabled("Votifier") || isPluginEnabled("NuVotifier")) {
            nuVotifierHook = new NuVotifierHook(plugin);
            nuVotifierHook.hook();
            plugin.getLogger().info("✓ NuVotifier integration enabled");
        }

        if (isPluginEnabled("zAuctionHouseV3")) {
            zAuctionHouseHook = new ZAuctionHouseHook(plugin);
            zAuctionHouseHook.hook();
            plugin.getLogger().info("✓ zAuctionHouse integration enabled");
        }

        if (isPluginEnabled("BountyHunters")) {
            bountyHuntersHook = new BountyHuntersHook(plugin);
            bountyHuntersHook.hook();
            plugin.getLogger().info("✓ BountyHunters integration enabled");
        }

        plugin.getLogger().info("Integration initialization complete!");
    }

    private boolean isPluginEnabled(String pluginName) {
        Plugin p = plugin.getServer().getPluginManager().getPlugin(pluginName);
        return p != null && p.isEnabled();
    }

    public CombatLogXHook getCombatLogXHook() {
        return combatLogXHook;
    }
}