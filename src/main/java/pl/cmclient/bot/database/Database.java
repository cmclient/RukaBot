package pl.cmclient.bot.database;

import lombok.RequiredArgsConstructor;
import pl.cmclient.bot.BotApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class Database {

    private final BotApplication bot;
    public final ExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private Connection connection;
    private boolean connected;

    public boolean connect() {
        try {
            String sqliteDatabaseName = bot.getConfig().getDatabaseName();
            bot.getLogger().info("Database: sqLite ({})", sqliteDatabaseName);
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDatabaseName);
            this.connected = true;
            return true;
        } catch (SQLException | ClassNotFoundException ex) {
            bot.getLogger().error("Failed to connect to database", ex);
            this.connected = false;
            return false;
        }
    }

    public void disconnect() throws SQLException {
        this.connection.close();
        this.connected = false;
    }

    public void update(String update) {
        if (!connected) return;
        executor.submit(() -> {
            try {
                connection.createStatement().executeUpdate(update);
            } catch (SQLException ex) {
                bot.getLogger().error("Failed to execute database update", ex);
            }
        });
    }

    public void query(String query, QueryCallback callback) {
        if (!this.connected) return;
        executor.submit(() -> {
            try (ResultSet rs = connection.createStatement().executeQuery(query)) {
                callback.receivedResultSet(rs);
            } catch (SQLException ex) {
                bot.getLogger().error("Failed to execute database query", ex);
            }
        });
    }

    public interface QueryCallback {
        void receivedResultSet(ResultSet rs);
    }
}