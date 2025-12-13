// ============================================================================
// FILE 2: ElementalMCBattlepassTracker.java
// LOCATION: src/main/java/com/elemental/battlepass/ElementalMCBattlepassTracker.java
// REPLACE ENTIRE FILE
// ============================================================================
package com.elemental.battlepass;

import com.elemental.battlepass.config.ConfigManager;
import com.elemental.battlepass.database.DatabaseManager;
import com.elemental.battlepass.integrations.IntegrationManager;
import com.elemental.battlepass.listeners.*;
import com.elemental.battlepass.managers.QuestManager;
import com.elemental.battlepass.managers.SeasonManager;
import com.elemental.battlepass.managers.StatTracker;
import com.elemental.battlepass.managers.TimerManager;
import com.elemental.battlepass.commands.BattlepassCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ElementalMCBattlepassTracker extends JavaPlugin {

    private static ElementalMCBattlepassTracker instance;
    
    private DatabaseManager databaseManager;
    private SeasonManager seasonManager;
    private QuestManager questManager;
    private StatTracker statTracker;
    private IntegrationManager integrationManager;
    private TimerManager timerManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        
        try {
            getLogger().info("Initializing ElementalBattlepassTracker...");
            
            configManager = new ConfigManager(this);
            databaseManager = new DatabaseManager(this);
            timerManager = new TimerManager(this);
            seasonManager = new SeasonManager(this);
            statTracker = new StatTracker(this);
            questManager = new QuestManager(this);
            integrationManager = new IntegrationManager(this);
            
            registerListeners();
            
            getCommand("battlepass").setExecutor(new BattlepassCommand(this));
            
            integrationManager.initialize();
            
            if (databaseManager.isConnected()) {
                seasonManager.loadActiveSeason();
                questManager.loadQuestsForActiveSeason();
            } else {
                getLogger().warning("Database not available - configure it in config.yml and reload!");
            }
            
            getLogger().info("ElementalBattlepassTracker has been enabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize plugin!", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down ElementalBattlepassTracker...");
        
        if (questManager != null) {
            questManager.shutdown();
        }
        
        if (statTracker != null) {
            statTracker.shutdown();
        }
        
        if (timerManager != null) {
            timerManager.shutdown();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("ElementalBattlepassTracker has been disabled!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
    }

    public static ElementalMCBattlepassTracker getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public StatTracker getStatTracker() {
        return statTracker;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}