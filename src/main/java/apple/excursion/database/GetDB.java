package apple.excursion.database;

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
        List<OldSubmission> submissions = new ArrayList<>();
        for (String submissionId : submissionIds) {
            statement = VerifyDB.submissionDbConnection.createStatement();
            sql = getSqlSubmissionGetAll(submissionId);
            response = statement.executeQuery(sql);
            int submissionIdInt = response.getInt(1);
            Long date = response.getLong(2);
            String taskName = response.getString(3);
            String links = response.getString(4);
            String submitter = response.getString(5);
            String otherSubmitters = response.getString(6);
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

    private static String getSqlPlayerGetAll(long id) {
        return String.format("SELECT * " +
                "FROM players " +
                "WHERE player_uid = '%d'", id);
    }
}
