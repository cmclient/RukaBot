package pl.cmclient.bot.database;

import pl.cmclient.bot.BotApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {

    public final ExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private Connection connection;
    private boolean connected;

    public boolean connect(BotApplication bot) {
        try {
            String sqliteDatabaseName = bot.getConfig().getSqliteDatabaseName();
            bot.getLogger().info("Database: sqLite (" + sqliteDatabaseName + ")");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteDatabaseName);
            this.connected = true;
            return true;
        } catch (SQLException | ClassNotFoundException ex) {
            bot.getLogger().error("Unable to connect to database", ex);
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void query(String query, QueryCallback callback) {
        if (!this.connected) return;
        executor.submit(() -> {
            try (ResultSet rs = connection.createStatement().executeQuery(query)) {
                callback.receivedResultSet(rs);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public interface QueryCallback {
        void receivedResultSet(ResultSet rs);
    }
}