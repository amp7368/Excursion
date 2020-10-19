package apple.excursion.database;

import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.Pair;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UpdateDB {
    public static void updateGuild(String guildName, String guildTag, long id, String playerName) throws SQLException, IOException {
        SheetsPlayerStats.updateGuild(guildName, guildTag, id, playerName);
        synchronized (VerifyDB.syncDB) {
            Statement statement = VerifyDB.database.createStatement();
            String sql = GetSql.getSqlExistsPlayer(id);
            ResultSet response = statement.executeQuery(sql);
            if (response.getInt(1) == 1) {
                // the player exists so update their guild_name and guild_tag
                sql = GetSql.getSqlUpdatePlayerGuild(id, guildName, guildTag);
                statement.execute(sql);
            } else {
                // the player doesn't exist so add an entry
                InsertDB.insertPlayer(new Pair<>(id, playerName), guildName, guildTag);
            }
            response.close();
            statement.close();
        }
    }

    public static void createGuild(String guildName, String guildTag) throws SQLException {
        // no sheet change because creating a guild doesn't change a person's guild
        synchronized (VerifyDB.syncDB) {
            Statement statement = VerifyDB.database.createStatement();
            String sql = GetSql.getSqlInsertGuild(guildName, guildTag);
            statement.execute(sql);
            statement.close();
        }
    }
}
