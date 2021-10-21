package pl.kuezeze.bot.database;

import pl.kuezeze.bot.BotApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {

    public final ExecutorService executor = Executors.newScheduledThreadPool(10);
    private Connection connection;

    public boolean connect(BotApplication bot) {
        try {
            bot.getLogger().info("Database: sqLite (rukabot.db)");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:rukabot.db");
            return true;
        } catch (SQLException | ClassNotFoundException ex) {
            bot.getLogger().error("Unable to connect to database", ex);
            return false;
        }
    }

    public void update(String update) {
        executor.submit(() -> {
            try {
                connection.createStatement().executeUpdate(update);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void query(String query, QueryCallback callback) {
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