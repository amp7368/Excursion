package apple.excursion.database;

import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class InsertSubmissionDB {
    // todo deal with this constant
    public static void insertSubmission(SubmissionData data) throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String monthName = getMonthName(data.getTime());
            String insertSql, updateSql, getSql, existsSql;
            Statement statement;

            // insert the new submission to the end of the table
            insertSql = GetSql.getSqlInsertSubmission(data);
            statement = VerifyDB.submissionDbConnection.createStatement();
            statement.execute(insertSql);
            statement.close();

            // for every person who submitted,
            // give points to the guild that many times,
            // give that many submissions to the guild
            // give that submission to every player
            // give that score and submission count in the leaderboard for every player
            for (Pair<Long, String> id : data.getSubmittersNameAndIds()) {
                // check if the player exists in the player DB
                existsSql = GetSql.getSqlExistsPlayer(id.getKey());
                statement = VerifyDB.playerDbConnection.createStatement();
                ResultSet response = statement.executeQuery(existsSql);
                boolean exists = 1 == response.getInt(1);
                response.close();
                if (exists) {
                    // if the player exist in the DB, then update their list of submissions
                    getSql = GetSql.getSqlGetPlayerSubmissionIds(id);
                    String submission_ids = statement.executeQuery(getSql).getString(1);
                    if (submission_ids.equals("")) {
                        // this shouldn't happen unless someone manually writes to the DB
                        submission_ids = String.valueOf(VerifyDB.currentSubmissionId);
                    } else {
                        submission_ids += "," + VerifyDB.currentSubmissionId;
                    }
                    updateSql = GetSql.getSqlUpdatePlayerSubmissionIds(id, submission_ids);
                    statement.execute(updateSql);
                } else {
                    // if the player doesn't exist, give them a new row with their first submission
                    insertSql =
                            GetSql.getSqlInsertPlayers(id, null, null, VerifyDB.currentSubmissionId);
                    statement.execute(insertSql);
                }

                // get the player guild from the playerDB
                getSql = GetSql.getSqlGetPlayerGuild(id);
                response = statement.executeQuery(getSql);
                String guildName = response.getString(1);
                String guildTag = response.getString(2);
                response.close();
                statement.close();
                if (guildName != null && guildTag != null) {
                    // check if the guild exists in the guild leaderboard
                    statement = VerifyDB.guildLbDbConnection.createStatement();
                    existsSql = GetSql.getSqlExistsLbGuild(guildTag, monthName);
                    response = statement.executeQuery(existsSql);
                    exists = 1 == response.getInt(1);
                    response.close();
                    if (exists) {
                        // if the player exist in the DB, then update their score and submission count
                        getSql = GetSql.getSqlGetLbGuild(guildName, guildTag, monthName);
                        response = statement.executeQuery(getSql);
                        int score = response.getInt(1);
                        int count = response.getInt(2);
                        response.close();
                        score += data.getTaskScore();
                        count++;
                        updateSql = GetSql.getSqlUpdateLbGuild(guildName, guildTag, monthName, score, count);
                        statement.execute(updateSql);
                    } else {
                        // if the player doesn't exist in the DB, then insert a new row
                        insertSql = GetSql.getSqlInsertLbGuild(guildName, guildTag, monthName, data.getTaskScore(), 1);
                        statement.execute(insertSql);
                    }

                    // check if the guild exists in the guild db
                    statement = VerifyDB.guildDbConnection.createStatement();
                    existsSql = GetSql.getSqlExistsGuild(guildTag);
                    response = statement.executeQuery(existsSql);
                    exists = 1 == response.getInt(1);
                    response.close();
                    if (!exists) {
                        insertSql = GetSql.getSqlInsertGuild(guildName, guildTag);
                        statement.execute(insertSql);
                    }
                    statement.close();
                }

                // check if the player exists in the player leaderboard
                statement = VerifyDB.playerLbDbConnection.createStatement();
                existsSql = GetSql.getSqlExistsLbPlayer(id, monthName);
                response = statement.executeQuery(existsSql);
                exists = 1 == response.getInt(1);
                response.close();

                if (exists) {
                    // if the player exist in the DB, then update their score and submission count
                    getSql = GetSql.getSqlGetLbPlayer(id, monthName);
                    response = statement.executeQuery(getSql);
                    int score = response.getInt(1);
                    int count = response.getInt(2);
                    response.close();
                    score += data.getTaskScore();
                    count++;
                    updateSql = GetSql.getSqlUpdateLbPlayer(id, monthName, score, count);
                    statement.execute(updateSql);
                } else {
                    // if the player doesn't exist in the DB, then insert a new row
                    insertSql = GetSql.getSqlInsertLbPlayers(id, monthName, data.getTaskScore(), 1);
                    statement.execute(insertSql);
                }
                statement.close();
            }
            VerifyDB.currentSubmissionId++;
        }
    }

    private static String getMonthName(long epochSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("MMM_yyyy");
        return formatter.format(new Date(epochSeconds * 1000));
    }
}
