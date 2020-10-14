package apple.excursion.database;

import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public class InsertSubmissionDB {
    // todo deal with this constant
    private static final String MONTH_NAME = "OCT_2020";

    public static void insertSubmission(SubmissionData data) throws SQLException {
        synchronized (VerifyDB.syncDB) {
            String insertSql, updateSql, getSql, existsSql;
            Statement statement;

            // insert the new submission to the end of the table
            insertSql = getSqlInsertSubmission(data);
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
                existsSql = getSqlExistsPlayer(id);
                statement = VerifyDB.playerDbConnection.createStatement();
                ResultSet response = statement.executeQuery(existsSql);
                boolean exists = 1 == response.getInt(1);
                if (exists) {
                    // if the player exist in the DB, then update their list of submissions
                    getSql = getSqlGetPlayerSubmissionIds(id);
                    String submission_ids = statement.executeQuery(getSql).getString(1);
                    if (submission_ids.equals("")) {
                        // this shouldn't happen unless someone manually writes to the DB
                        submission_ids = String.valueOf(VerifyDB.currentSubmissionId);
                    } else {
                        submission_ids += "," + VerifyDB.currentSubmissionId;
                    }
                    updateSql = getSqlUpdatePlayerSubmissionIds(id, submission_ids);
                    statement.execute(updateSql);
                } else {
                    // if the player doesn't exist, give them a new row with their first submission
                    insertSql =
                            getSqlInsertPlayers(id);
                    statement.execute(insertSql);
                }

                // get the player guild from the playerDB
                getSql = getSqlGetPlayerGuild(id);
                response = statement.executeQuery(getSql);
                String guildName = response.getString(1);
                String guildTag = response.getString(2);
                statement.close();
                if (guildName != null && guildTag != null) {
                    // check if the guild exists in the guild leaderboard
                    statement = VerifyDB.guildLbDbConnection.createStatement();
                    existsSql = getSqlExistsLbGuild(guildName, guildTag, MONTH_NAME);
                    response = statement.executeQuery(existsSql);
                    exists = 1 == response.getInt(1);
                    if (exists) {
                        // if the player exist in the DB, then update their score and submission count
                        getSql = getSqlGetLbGuild(guildName, guildTag, MONTH_NAME);
                        response = statement.executeQuery(getSql);
                        int score = response.getInt(1);
                        int count = response.getInt(2);
                        score += data.getTaskScore();
                        count++;
                        updateSql = getSqlUpdateLbGuild(guildName, guildTag, MONTH_NAME, score, count);
                        statement.execute(updateSql);
                    } else {
                        // if the player doesn't exist in the DB, then insert a new row
                        insertSql = getSqlInsertLbGuild(guildName, guildTag, MONTH_NAME, data.getTaskScore(), 1);
                        statement.execute(insertSql);
                    }
                    statement.close();
                }

                // check if the player exists in the player leaderboard
                statement = VerifyDB.playerLbDbConnection.createStatement();
                existsSql = getSqlExistsLbPlayer(id, MONTH_NAME);
                response = statement.executeQuery(existsSql);
                exists = 1 == response.getInt(1);

                if (exists) {
                    // if the player exist in the DB, then update their score and submission count
                    getSql = getSqlGetLbPlayer(id, MONTH_NAME);
                    response = statement.executeQuery(getSql);
                    int score = response.getInt(1);
                    int count = response.getInt(2);
                    score += data.getTaskScore();
                    count++;
                    updateSql = getSqlUpdateLbPlayer(id, MONTH_NAME, score, count);
                    statement.execute(updateSql);
                } else {
                    // if the player doesn't exist in the DB, then insert a new row
                    insertSql = getSqlInsertLbPlayers(id, MONTH_NAME, data.getTaskScore(), 1);
                    statement.execute(insertSql);
                }
                statement.close();
            }
            VerifyDB.currentSubmissionId++;
        }
    }


    // all the exists sql


    @NotNull
    private static String getSqlExistsLbGuild(String guildName, String guildTag, String monthName) {
        return String.format("SELECT COUNT(1) " +
                "FROM %s " +
                "WHERE (guild_name = '%s' AND guild_tag = '%s');",
                monthName, guildName, guildTag);
    }


    @NotNull
    private static String getSqlExistsLbPlayer(Pair<Long, String> id, String monthName) {
        return "SELECT COUNT(1)" +
                "FROM " + monthName + " "
                + "WHERE player_uid = '"
                + id.getKey()
                + "';";
    }

    @NotNull
    private static String getSqlExistsPlayer(Pair<Long, String> id) {
        return "SELECT COUNT(1)" +
                "FROM players " +
                "WHERE player_uid = '"
                + id.getKey()
                + "';";
    }

    // all the insert sql

    @NotNull
    private static String getSqlInsertPlayers(Pair<Long, String> id) {
        return "INSERT INTO players(player_uid, player_name, submission_ids) "
                + "VALUES "
                + String.format("('%d','%s','%d');",
                id.getKey(),
                id.getValue(),
                VerifyDB.currentSubmissionId
        );
    }

    @NotNull
    private static String getSqlInsertSubmission(SubmissionData data) {
        final Collection<String> linksList = data.getLinks();
        String links;
        if (linksList.isEmpty())
            links = null;
        else
            links = "'" + String.join(",", linksList) + "'";

        return "INSERT INTO submissions(id, date_submitted, task_name, links, submitter, all_submitters) "
                + "VALUES "
                + String.format("(%d,'%s','%s',%s,'%s',%s);",
                VerifyDB.currentSubmissionId,
                data.getTime(),
                data.getTaskName(),
                links,
                data.getSubmitterId(),
                data.getOtherSubmitters() == null ? null : "'" + data.getOtherSubmitters() + "'"
        );
    }

    @NotNull
    private static String getSqlInsertLbPlayers(Pair<Long, String> id, String monthName, int taskScore, int count) {
        return String.format("INSERT INTO %s (player_uid, score, submissions_count) "
                        + "VALUES ('%d', %d, %d);",
                monthName, id.getKey(), taskScore, count
        );
    }

    @NotNull
    private static String getSqlInsertLbGuild(String guildName, String guildTag, String monthName, int taskScore, int count) {
        return String.format("INSERT INTO %s (guild_tag, guild_name, score, submissions_count) "
                        + "VALUES ('%s', '%s', %d, %d);"
                , monthName, guildTag, guildName, taskScore, count);
    }


    // all the get sql

    @NotNull
    private static String getSqlGetLbGuild(String guildName, String guildTag, String monthName) {
        return String.format(
                "SELECT score, submissions_count "
                        + "FROM %s "
                        + "WHERE (guild_name = '%s' AND guild_tag = '%s');"
                , monthName, guildName, guildTag
        );
    }

    @NotNull
    private static String getSqlGetPlayerSubmissionIds(Pair<Long, String> id) {
        return "SELECT submission_ids " +
                "FROM players " +
                "WHERE player_uid = " + id.getKey();
    }

    @NotNull
    private static String getSqlGetPlayerGuild(Pair<Long, String> id) {
        return "SELECT guild_name, guild_tag " +
                "FROM players " +
                "WHERE player_uid = " + id.getKey();
    }

    @NotNull
    private static String getSqlGetLbPlayer(Pair<Long, String> id, String monthName) {
        return "SELECT score, submissions_count " +
                "FROM " + monthName + " "
                + "WHERE player_uid = '"
                + id.getKey()
                + "';";
    }

    // all the update sql


    @NotNull
    private static String getSqlUpdatePlayerSubmissionIds(Pair<Long, String> id, String submission_ids) {
        return "UPDATE players "
                + "SET submission_ids = '"
                + submission_ids
                + "' WHERE player_uid = "
                + id.getKey()
                + ";";
    }

    @NotNull
    private static String getSqlUpdateLbPlayer(Pair<Long, String> id, String monthName, int score, int count) {
        return String.format("UPDATE %s "
                        + "SET score = %d, submissions_count = %d "
                        + "WHERE player_uid = %d;"
                , monthName, score, count, id.getKey());
    }

    @NotNull
    private static String getSqlUpdateLbGuild(String guildName, String guildTag, String monthName, int score, int count) {
        return String.format("UPDATE %s "
                        + "SET score = %d, submissions_count = %d "
                        + "WHERE (guild_name = '%s' AND guild_tag = '%s');"
                , monthName, score, count, guildName, guildTag
        );
    }
}
