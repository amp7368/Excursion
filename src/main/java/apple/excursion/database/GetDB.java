package apple.excursion.database;

import apple.excursion.utils.Pair;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GetDB {
    public static PlayerData getPlayerData(long id) throws SQLException {
        String sql = getSqlPlayerGetAll(id);
        Statement statement = VerifyDB.playerDbConnection.createStatement();
        ResultSet response = statement.executeQuery(sql);
        String playerName = response.getString(2);
        String guildName = response.getString(3);
        String guildTag = response.getString(4);
        String[] submissionIds = response.getString(5).split(",");
        response.close();
        List<OldSubmission> submissions = new ArrayList<>();
        for (String submissionId : submissionIds) {
            statement = VerifyDB.submissionDbConnection.createStatement();
            sql = getSqlSubmissionGetAll(submissionId);
            response = statement.executeQuery(sql);
            int submissionIdInt = response.getInt(1);
            Long date = response.getLong(2);
            String taskName = response.getString(3);
            String links = response.getString(4);
            String submitterId = response.getString(5);
            String otherSubmittersIdsListAsString = response.getString(6);
            response.close();

            statement = VerifyDB.playerDbConnection.createStatement();
            String[] otherSubmittersIds = otherSubmittersIdsListAsString == null ? null : otherSubmittersIdsListAsString.split(",");
            List<Pair<String, String>> otherSubmitters = new ArrayList<>();
            if (otherSubmittersIds != null)
                for (String otherSubmitterId : otherSubmittersIds) {
                    sql = getSqlPlayerGetName(otherSubmitterId);
                    response = statement.executeQuery(sql);
                    otherSubmitters.add(new Pair<>(otherSubmitterId, response.getString(1)));
                    response.close();
                }
            sql = getSqlPlayerGetName(submitterId);
            response = statement.executeQuery(sql);
            Pair<String, String> submitter = new Pair<>(submitterId, response.getString(1));
            response.close();

            submissions.add(new OldSubmission(
                    submissionIdInt,
                    date,
                    taskName,
                    links,
                    submitter,
                    otherSubmitters
            ));
        }
        return new PlayerData(id, playerName, guildName, guildTag, submissions);
    }


    private static String getSqlSubmissionGetAll(String submissionId) {
        return String.format("SELECT * " +
                "FROM submissions " +
                "WHERE id = %s;", submissionId);
    }

    private static String getSqlPlayerGetName(String id) {
        return String.format("SELECT player_name " +
                "FROM players " +
                "WHERE player_uid = '%s';", id);
    }

    private static String getSqlPlayerGetAll(long id) {
        return String.format("SELECT * " +
                "FROM players " +
                "WHERE player_uid = '%d';", id);
    }
}
