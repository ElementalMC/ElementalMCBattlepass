// ============================================================================
// FILE 1: DatabaseManager.java
// LOCATION: src/main/java/com/elemental/battlepass/database/DatabaseManager.java
// REPLACE ENTIRE FILE
// ============================================================================
package com.elemental.battlepass.database;

import com.elemental.battlepass.ElementalMCBattlepassTracker;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final ElementalMCBattlepassTracker plugin;
    private HikariDataSource dataSource;
    private boolean connected = false;

    public DatabaseManager(ElementalMCBattlepassTracker plugin) {
        this.plugin = plugin;
        attemptConnection();
    }

    public boolean attemptConnection() {
        FileConfiguration config = plugin.getConfig();
        
        String host = config.getString("database.host", "");
        String database = config.getString("database.database", "");
        String username = config.getString("database.username", "");
        String password = config.getString("database.password", "");
        
        if (host.isEmpty() || database.isEmpty()) {
            plugin.getLogger().warning("Database not configured! Plugin will run without database support.");
            plugin.getLogger().warning("Configure database in config.yml and use /battlepass reload to connect.");
            connected = false;
            return false;
        }

        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" +
                config.getInt("database.port", 3306) + "/" + database +
                "?createDatabaseIfNotExist=true&autoReconnect=true&useSSL=false");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            
            hikariConfig.setMaximumPoolSize(config.getInt("database.pool.maximum-pool-size", 10));
            hikariConfig.setMinimumIdle(config.getInt("database.pool.minimum-idle", 2));
            hikariConfig.setMaxLifetime(config.getLong("database.pool.max-lifetime", 1800000));
            hikariConfig.setConnectionTimeout(config.getLong("database.pool.connection-timeout", 30000));
            hikariConfig.setIdleTimeout(config.getLong("database.pool.idle-timeout", 600000));
            
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
            hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
            hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
            hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
            hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
            hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
            hikariConfig.addDataSourceProperty("maintainTimeStats", "false");
            
            this.dataSource = new HikariDataSource(hikariConfig);
            
            try (Connection conn = dataSource.getConnection()) {
                connected = true;
                plugin.getLogger().info("✓ Database connection established successfully!");
                createTables();
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect to database: " + e.getMessage());
            plugin.getLogger().warning("Plugin will continue without database support.");
            plugin.getLogger().warning("Fix database configuration and use /battlepass reload to reconnect.");
            connected = false;
            
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
            }
            
            return false;
        }
    }

    private void createTables() {
        if (!isConnected()) return;

        plugin.getLogger().info("Setting up database tables...");
        
        String[] schemas = {
            "CREATE TABLE IF NOT EXISTS players (" +
            "uuid VARCHAR(36) PRIMARY KEY," +
            "username VARCHAR(16) NOT NULL," +
            "last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "INDEX idx_username (username)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS seasons (" +
            "season_id INT AUTO_INCREMENT PRIMARY KEY," +
            "season_name VARCHAR(64) NOT NULL," +
            "start_date TIMESTAMP NOT NULL," +
            "end_date TIMESTAMP NULL," +
            "active BOOLEAN DEFAULT FALSE," +
            "INDEX idx_active (active)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS quests (" +
            "quest_id VARCHAR(64) NOT NULL," +
            "season_id INT NOT NULL," +
            "quest_type VARCHAR(64) NOT NULL," +
            "target VARCHAR(128)," +
            "required_amount INT NOT NULL," +
            "time_limit_seconds INT," +
            "reset_on_fail BOOLEAN DEFAULT FALSE," +
            "display_name VARCHAR(128)," +
            "description TEXT," +
            "PRIMARY KEY (quest_id, season_id)," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE," +
            "INDEX idx_season_type (season_id, quest_type)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS player_progress (" +
            "uuid VARCHAR(36) NOT NULL," +
            "season_id INT NOT NULL," +
            "quest_id VARCHAR(64) NOT NULL," +
            "progress INT DEFAULT 0," +
            "start_timestamp BIGINT," +
            "completed BOOLEAN DEFAULT FALSE," +
            "completed_at TIMESTAMP NULL," +
            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (uuid, season_id, quest_id)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE," +
            "INDEX idx_completed (completed)," +
            "INDEX idx_season_quest (season_id, quest_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS player_stats (" +
            "uuid VARCHAR(36) NOT NULL," +
            "season_id INT NOT NULL," +
            "stat_type VARCHAR(64) NOT NULL," +
            "stat_target VARCHAR(128) DEFAULT ''," +
            "amount BIGINT DEFAULT 0," +
            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
            "PRIMARY KEY (uuid, season_id, stat_type, stat_target)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE," +
            "INDEX idx_stat_type (stat_type)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS player_tiers (" +
            "uuid VARCHAR(36) NOT NULL," +
            "season_id INT NOT NULL," +
            "tier VARCHAR(32) NOT NULL DEFAULT 'free'," +
            "purchased_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "PRIMARY KEY (uuid, season_id)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS player_playtime (" +
            "uuid VARCHAR(36) NOT NULL," +
            "season_id INT NOT NULL," +
            "session_start BIGINT NOT NULL," +
            "session_end BIGINT," +
            "duration_seconds INT," +
            "PRIMARY KEY (uuid, season_id, session_start)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE," +
            "INDEX idx_sessions (uuid, season_id, session_start)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;",
            
            "CREATE TABLE IF NOT EXISTS player_login_streak (" +
            "uuid VARCHAR(36) NOT NULL," +
            "season_id INT NOT NULL," +
            "current_streak INT DEFAULT 0," +
            "longest_streak INT DEFAULT 0," +
            "last_login_date DATE," +
            "PRIMARY KEY (uuid, season_id)," +
            "FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
            "FOREIGN KEY (season_id) REFERENCES seasons(season_id) ON DELETE CASCADE" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
        };

        int successCount = 0;
        for (String schema : schemas) {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(schema)) {
                stmt.executeUpdate();
                successCount++;
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create table", e);
            }
        }
        
        plugin.getLogger().info("✓ Database tables ready! (" + successCount + "/" + schemas.length + " created/verified)");
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            throw new SQLException("Database is not connected! Configure database in config.yml");
        }
        
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        
        return dataSource.getConnection();
    }

    public CompletableFuture<Void> executeAsync(String sql, Object... params) {
        if (!isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().log(Level.WARNING, "Database async execution failed: " + sql, e);
                }
            }
        });
    }

    public boolean isConnected() {
        return connected && dataSource != null && !dataSource.isClosed();
    }

    public void reconnect() {
        plugin.getLogger().info("Attempting to reconnect to database...");
        attemptConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            connected = false;
            plugin.getLogger().info("Database connection closed!");
        }
    }
}