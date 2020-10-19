package apple.excursion.database;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.database.objects.player.PlayerLeaderboardEntry;
import apple.excursion.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetDB {
    public static PlayerData getPlayerData(Pair<Long, String> id, int submissionSize) throws SQLException {
        synchronized (VerifyDB.syncDB) {
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
                return new PlayerData(id.getKey(), id.getValue(), null, null, new ArrayList<>(), 0, 0);
            }
            // if the player has the wrong playerName
            if (!playerName.equals(id.getValue())) {
                sql = GetSql.updatePlayerName(id.getKey(), id.getValue());
                statement.execute(sql);
                playerName = id.getValue();
                // reget the playerdata because the response closes somehow. probably because statement.execute();
                sql = GetSql.getSqlGetPlayerAll(id.getKey());
                response = statement.executeQuery(sql);
            }

            int score = response.getInt(1);
            String guildTag = response.getString(3);
            String guildName = response.getString(4);
            int soulJuice = response.getInt(5);
            response.close();
            List<OldSubmission> submissions = new ArrayList<>();
            sql = GetSql.getSqlGetPlayerSubmissionHistory(id.getKey(), submissionSize);
            response = statement.executeQuery(sql);
            while (response.next()) {
                submissions.add(new OldSubmission(response));
            }

            statement.close();
            response.close();

            return new PlayerData(id.getKey(),playerName, guildName, guildTag, submissions, score, soulJuice);
        }
    }


    public static LeaderboardOfGuilds getGuildLeaderboard() throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String sql = GetSql.getSqlGetGuilds();
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            List<GuildLeaderboardEntry> guilds = new ArrayList<>();
            if (!response.isClosed())
                while (response.next()) {
                    int guildScore = response.getInt(1);
                    String guildTag = response.getString(2);
                    String guildName = response.getString(3);
                    String playerName = response.getString(4);
                    int playerScore = response.getInt(5);
                    guilds.add(new GuildLeaderboardEntry(guildTag, guildName, guildScore, playerName, playerScore));
                }
            response.close();
            statement.close();
            return new LeaderboardOfGuilds(guilds);
        }
    }

    public static List<PlayerData> getPlayersInGuild(String tag) throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String sql = GetSql.getSqlGetPlayersInGuild(tag);
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            List<PlayerData> players = new ArrayList<>();
            response.next();
            while (!response.isClosed()) {
                long id = response.getLong(1);
                String playerName = response.getString(2);
                String guildTag = response.getString(3);
                String guildName = response.getString(4);
                int score = response.getInt(5);
                int soulJuice = response.getInt(6);
                players.add(new PlayerData(
                        id,
                        playerName,
                        guildName,
                        guildTag,
                        null,
                        score,
                        soulJuice
                )); // submissions is normally not null, but for this situation we don't need them
                response.next();
            }
            response.close();
            statement.close();
            return players;
        }
    }

    public static List<GuildHeader> getGuildNameList() throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String sql = GetSql.getSqlGetGuildNames();
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            List<GuildHeader> guilds = new ArrayList<>();
            if (!response.isClosed())
                while (response.next()) {
                    String guildTag = response.getString(1);
                    String guildName = response.getString(2);
                    guilds.add(new GuildHeader(guildTag, guildName));
                }
            response.close();
            statement.close();
            return guilds;
        }
    }

    public static List<OldSubmission> getGuildSubmissions(String guildTag) throws SQLException {
        List<OldSubmission> submissions = new ArrayList<>();
        String sql = GetSql.getSqlGetGuildSubmissionHistory(guildTag);
        Statement statement = VerifyDB.database.createStatement();
        ResultSet response = statement.executeQuery(sql);
        while (response.next()) {
            submissions.add(new OldSubmission(response));
        }

        statement.close();
        response.close();
        return submissions;
    }

    public static PlayerLeaderboard getPlayerLeaderboard() throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String sql = GetSql.getSqlGetPlayerLeaderboard();
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            List<PlayerLeaderboardEntry> leaderboard = new ArrayList<>();
            if (!response.isClosed())
                while (response.next()) {
                    leaderboard.add(new PlayerLeaderboardEntry(response));
                }
            return new PlayerLeaderboard(leaderboard);
        }
    }

    public static List<PlayerHeader> getPlayerHeaders() throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String sql = GetSql.getSqlGetPlayerHeaders();
            Statement statement = VerifyDB.database.createStatement();
            ResultSet response = statement.executeQuery(sql);
            List<PlayerHeader> players = new ArrayList<>();
            if (!response.isClosed())
                while (response.next()) {
                    players.add(new PlayerHeader(response.getLong(1),
                            response.getString(2),
                            response.getInt(3),
                            response.getInt(4)));
                }
            sql = GetSql.getSqlGetPlayerHeadersNoScore();
            response = statement.executeQuery(sql);
            if (!response.isClosed())
                while (response.next()) {
                    long id = response.getLong(1);
                    boolean contains = false;
                    for (PlayerHeader header : players)
                        if (header.id == id) {
                            contains = true;
                            break;
                        }
                    if (!contains)
                        players.add(new PlayerHeader(id,
                                response.getString(2),
                                response.getInt(3),
                                0));
                }
            statement.close();
            return players;
        }

    }
}
