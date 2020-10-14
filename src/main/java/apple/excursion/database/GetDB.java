package apple.excursion.database;

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
            sql = GetSql.getSqlInsertPlayers(id,-1);
            statement.execute(sql);
            response.close();
            statement.close();
            return new PlayerData(id.getKey(), id.getValue(), null, null, new ArrayList<>());
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
        String[] submissionIds = response.getString(5).split(",");
        response.close();
        statement.close();
        List<OldSubmission> submissions = new ArrayList<>();
        for (String submissionId : submissionIds) {
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

            submissions.add(new OldSubmission(
                    submissionIdInt,
                    date,
                    taskName,
                    links,
                    submitter,
                    otherSubmitters
            ));
        }
        return new PlayerData(id.getKey(), playerName, guildName, guildTag, submissions);
    }


}
