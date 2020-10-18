package apple.excursion.database;

import apple.excursion.database.objects.GuildData;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.PlayerData;
import apple.excursion.discord.data.answers.GuildLeaderboardEntry;
import apple.excursion.discord.data.answers.LeaderboardOfGuilds;
import apple.excursion.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetDB {
    public static PlayerData getPlayerData(Pair<Long, String> id) throws SQLException {
        String sql = GetSql.getSqlGetPlayerAll(id.getKey());
        Statement statement = VerifyDB.database.createStatement();
        ResultSet response = statement.executeQuery(sql);
        String playerName;
        // if the player doesn't exist
        if (response.isClosed() || (playerName = response.getString(2)) == null) {
            // add the player
            InsertDB.insertPlayer(id, null, null);
            response.close();
            statement.close();
            return new PlayerData(id.getValue(), null, null, new ArrayList<>(), 0);
        }

        // if the player has the wrong playerName
        if (!playerName.equals(id.getValue())) {
            sql = GetSql.updatePlayerName(id.getKey(), id.getValue());
            statement.execute(sql);
            playerName = id.getValue();
        }

        int score = response.getInt(1);
        String guildTag = response.getString(3);
        String guildName = response.getString(4);
        response.close();
        List<OldSubmission> submissions = new ArrayList<>();
        sql = GetSql.getSqlGetPlayerSubmissionHistory(id.getKey());
        response = statement.executeQuery(sql);
        while (response.next()) {
            submissions.add(new OldSubmission(response));
        }

        statement.close();
        response.close();

        return new PlayerData(playerName, guildName, guildTag, submissions, score);
    }


    public static LeaderboardOfGuilds getGuildList() throws SQLException {
        String sql = GetSql.getSqlGetGuilds();
        Statement statement = VerifyDB.database.createStatement();
        ResultSet response = statement.executeQuery(sql);
        List<GuildLeaderboardEntry> guilds = new ArrayList<>();
        if(!response.isClosed())
            response.next();
        while (!response.isClosed()) {
            int guildScore = response.getInt(1);
            String guildTag = response.getString(2);
            String guildName = response.getString(3);
            String playerName = response.getString(4);
            int playerScore = response.getInt(5);
            guilds.add(new GuildLeaderboardEntry(guildTag, guildName, guildScore, playerName, playerScore));
            response.next();
        }
        response.close();
        statement.close();

        return new LeaderboardOfGuilds(guilds);
    }


    public static List<PlayerData> getPlayersInGuild(String tag) throws SQLException {
        String sql = GetSql.getSqlGetPlayersInGuild(tag);
        Statement statement = VerifyDB.database.createStatement();
        ResultSet response = statement.executeQuery(sql);
        List<PlayerData> players = new ArrayList<>();
        response.next();
        while (!response.isClosed()) {
            String playerName = response.getString(1);
            String guildTag = response.getString(2);
            String guildName = response.getString(3);
            int score = response.getInt(4);
            players.add(new PlayerData(
                    playerName,
                    guildName,
                    guildTag,
                    null,
                    score
            )); // submissions is normally not null, but for this situation we don't need them
            response.next();
        }
        response.close();
        statement.close();
        return players;
    }
}
