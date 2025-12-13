
// ============================================================================
// FILE: DatabaseManager.java
// LOCATION: src/main/java/com/elemental/battlepass/database/
// ============================================================================
package com.elemental.battlepass.database;

import com.elemental.battlepass.ElementalBattlepassTracker;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DatabaseManager {

    private final ElementalBattlepassTracker plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(ElementalBattlepassTracker plugin) {
        this.plugin = plugin;
        initialize();
        createTables();
    }

    private void initialize() {
        FileConfiguration config = plugin.getConfig();
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + 
            config.getString("database.host", "localhost") + ":" +
            config.getInt("database.port", 3306) + "/" +
            config.getString("database.database", "battlepass"));
        hikariConfig.setUsername(config.getString("database.username", "root"));
        hikariConfig.setPassword(config.getString("database.password", ""));
        
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
        
        plugin.getLogger().info("Database connection pool initialized successfully!");
    }

    private void createTables() {
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

        for (String schema : schemas) {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(schema)) {
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create table", e);
            }
        }
        
        plugin.getLogger().info("Database tables created/verified successfully!");
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("DataSource is not initialized or closed");
        }
        return dataSource.getConnection();
    }

    public CompletableFuture<Void> executeAsync(String sql, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Database async execution failed: " + sql, e);
            }
        });
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Database connection pool closed!");
        }
    }
}