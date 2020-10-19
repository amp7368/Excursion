package apple.excursion.database;

import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static apple.excursion.database.VerifyDB.DEFAULT_GUILD_NAME;
import static apple.excursion.database.VerifyDB.DEFAULT_GUILD_TAG;

public class InsertDB {
    // todo deal with this constant
    public static void insertSubmission(SubmissionData data) throws SQLException {
        VerifyDB.verify();
        synchronized (VerifyDB.syncDB) {
            if (data.getType() == SubmissionData.TaskSubmissionType.DAILY) {
                VerifyDB.verifyCalendar();
            }
            String insertSql, getSql;
            ResultSet response;

            // insert the new submission in the table
            insertSql = GetSql.getSqlInsertSubmission(data);
            Statement statement = VerifyDB.database.createStatement();
            statement.execute(insertSql);

            for (Pair<Long, String> id : data.getSubmittersNameAndIds()) {
                getSql = GetSql.getSqlGetPlayerGuild(id.getKey());
                response = statement.executeQuery(getSql);
                String guildTag;
                if (response.isClosed()) {
                    insertPlayer(id, DEFAULT_GUILD_TAG, DEFAULT_GUILD_NAME);
                    guildTag = DEFAULT_GUILD_TAG;
                } else {
                    guildTag = response.getString(2);
                }
                response.close();
                insertSql = GetSql.getSqlInsertSubmissionLink(VerifyDB.currentSubmissionId, id.getKey(), guildTag);
                statement.execute(insertSql);
            }
            statement.executeBatch();
            statement.close();
            VerifyDB.currentSubmissionId++;
        }
    }

    static void insertPlayer(Pair<Long, String> id, String guildTag, String guildName) throws SQLException {
        Statement statement = VerifyDB.database.createStatement();
        statement.execute(GetSql.getSqlInsertPlayers(id, guildName, guildTag));
        statement.close();
    }

}
