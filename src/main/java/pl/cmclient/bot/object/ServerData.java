package pl.cmclient.bot.object;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.database.Database;
import pl.cmclient.bot.helper.StringHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private final long serverId;
    private final Database database;
    private boolean inviteBans;
    private final Map<User, PermissionType> userPermissions;
    private final List<String> bannedWords;

    public ServerData(long serverId) {
        this.serverId = serverId;
        this.userPermissions = new ConcurrentHashMap<>();
        this.bannedWords = new ArrayList<>();
        this.database = BotApplication.getInstance().getDatabase();
        this.insert();
    }

    public ServerData(ResultSet rs) throws SQLException {
        this.serverId = rs.getLong("serverId");
        this.inviteBans = rs.getBoolean("inviteBans");
        this.bannedWords = new ArrayList<>(List.of(rs.getString("bannedWords").split(",")));
        this.userPermissions = new ConcurrentHashMap<>();
        this.database = BotApplication.getInstance().getDatabase();
    }

    public long getServerId() {
        return serverId;
    }

    public boolean isInviteBans() {
        return inviteBans;
    }

    public void setInviteBans(boolean inviteBans) {
        this.inviteBans = inviteBans;
        this.database.update("UPDATE `servers` SET " +
                "`inviteBans`='" + (this.inviteBans ? 1 : 0) + "' WHERE `serverId` = '" + this.serverId + "'");
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }

    public void addBannedWord(String s) {
        this.bannedWords.add(s);
        this.database.update("UPDATE `servers` SET " +
                "`bannedWords`='" + StringHelper.join(this.bannedWords, ",") + "' WHERE `serverId` = '" + this.serverId + "'");
    }

    public void removeBannedWord(String s) {
        this.bannedWords.remove(s);
        this.database.update("UPDATE `servers` SET " +
                "`bannedWords`='" + StringHelper.join(this.bannedWords, ",") + "' WHERE `serverId` = '" + this.serverId + "'");
    }

    public Map<User, PermissionType> getUserPermissions() {
        return userPermissions;
    }

    private void insert() {
        this.database.update("INSERT INTO `servers`(`id`, `serverId`, `inviteBans`, `bannedWords`) VALUES (NULL, '" + this.serverId + "', '" + (this.inviteBans ? 1 : 0) + "', '');");
    }
}
