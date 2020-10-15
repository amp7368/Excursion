package apple.excursion.database;

import apple.excursion.database.objects.GuildData;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.PlayerData;
import apple.excursion.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetDB {
    public static PlayerData getPlayerData(Pair<Long, String> id) throws SQLException {
        String sql = GetSql.getSqlGetPlayerAll(id.getKey());
        Statement statement = VerifyDB.playerDbConnection.createStatement();
        ResultSet response = statement.executeQuery(sql);
        // if the player doesn't exist
        if (response.isClosed()) {
            // add the player
            sql = GetSql.getSqlInsertPlayers(id, null, null, -1);
            statement.execute(sql);
            response.close();
            statement.close();
            return new PlayerData(id.getKey(), id.getValue(), null, null, new ArrayList<>(), 0);
        }
        String playerName = response.getString(2);

        // if the player has the wrong playerName
        if (!playerName.equals(id.getValue())) {
            sql = GetSql.updatePlayerName(id.getKey(), id.getValue());
            statement.execute(sql);
            playerName = id.getValue();
        }

        String guildName = response.getString(3);
        String guildTag = response.getString(4);
        String submissionIds = response.getString(5);
        int score = response.getInt(6);
        response.close();
        statement.close();
        List<OldSubmission> submissions = new ArrayList<>();
        if (!submissionIds.isBlank())
            for (String submissionId : submissionIds.split(",")) {
                submissions.add(getOldSubmission(submissionId));
            }
        return new PlayerData(id.getKey(), playerName, guildName, guildTag, submissions, score);
    }

    public static OldSubmission getOldSubmission(String submissionId) throws SQLException {
        Statement statement;
        ResultSet response;
        String sql;
        statement = VerifyDB.submissionDbConnection.createStatement();
        sql = GetSql.getSqlSubmissionGetAll(submissionId);
        response = statement.executeQuery(sql);
        int submissionIdInt = response.getInt(1);
        Long date = response.getLong(2);
        String taskName = response.getString(3);
        String links = response.getString(4);
        String submitterId = response.getString(5);
        String otherSubmittersIdsListAsString = response.getString(6);
        response.close();
        statement.close();

        statement = VerifyDB.playerDbConnection.createStatement();
        String[] otherSubmittersIds = otherSubmittersIdsListAsString == null ? null : otherSubmittersIdsListAsString.split(",");
        List<Pair<String, String>> otherSubmitters = new ArrayList<>();
        if (otherSubmittersIds != null)
            for (String otherSubmitterId : otherSubmittersIds) {
                sql = GetSql.getSqlGetPlayerName(otherSubmitterId);
                response = statement.executeQuery(sql);
                otherSubmitters.add(new Pair<>(otherSubmitterId, response.getString(1)));
                response.close();
            }
        sql = GetSql.getSqlGetPlayerName(submitterId);
        response = statement.executeQuery(sql);
        Pair<String, String> submitter = new Pair<>(submitterId, response.getString(1));
        response.close();
        statement.close();

        return new OldSubmission(
                submissionIdInt,
                date,
                taskName,
                links,
                submitter,
                otherSubmitters
        );
    }


    public static List<GuildData> getGuildList() throws SQLException {
        String sql = GetSql.getSqlGetGuilds();
        Statement statement = VerifyDB.guildDbConnection.createStatement();
        ResultSet response = statement.executeQuery(sql);
        List<GuildData> guildData = new ArrayList<>();
        while (!response.isClosed()) {
            String tag = response.getString(1);
            String name = response.getString(2);
            String submissionsListAsString = response.getString(3);
            List<OldSubmission> submissions = new ArrayList<>();
            if (!submissionsListAsString.isBlank())
                for (String submission : submissionsListAsString.split(",")) {
                    submissions.add(GetDB.getOldSubmission(submission));
                }
            guildData.add(new GuildData(tag, name, submissions));
            response.next();
        }
        response.close();
        statement.close();
        return guildData;
    }


    public static List<PlayerData> getPlayersInGuild(String tag) throws SQLException {
        String sql = GetSql.getSqlGetPlayersInGuild(tag);
        Statement statement = VerifyDB.playerDbConnection.createStatement();
        ResultSet response = statement.executeQuery(sql);
        List<PlayerData> players = new ArrayList<>();
        response.next();
        while (!response.isClosed()) {
            long playerId = response.getLong(1);
            String playerName = response.getString(2);
            String guildName = response.getString(3);
            String guildTag = response.getString(4);
            int score = response.getInt(6);
            players.add(new PlayerData(
                    playerId,
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
