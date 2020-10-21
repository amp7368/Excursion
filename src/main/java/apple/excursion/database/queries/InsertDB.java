package apple.excursion.database.queries;

import apple.excursion.database.VerifyDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.User;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static apple.excursion.database.VerifyDB.*;

public class InsertDB {
    private static final int SOUL_JUICE_FOR_DAILY = 1;

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

            int soulJuice;
            if (data.getType() == SubmissionData.TaskSubmissionType.NORMAL)
                soulJuice = 0;
            else if (data.getType() == SubmissionData.TaskSubmissionType.DAILY)
                soulJuice = SOUL_JUICE_FOR_DAILY;
            else
                soulJuice = 0;

            for (Pair<Long, String> id : data.getSubmittersNameAndIds()) {
                try {
                    SheetsPlayerStats.submit(data.getTaskName(), id.getKey(), id.getValue(), soulJuice);
                } catch (IOException e) {
                    final User user = DiscordBot.client.getUserById(id.getKey());
                    if (user == null || user.isBot()) continue;
                    user.openPrivateChannel().complete().sendMessage("There was an error making your profile. Tell appleptr16 or ojomFox: " + e.getMessage()).queue();
                }
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
                insertSql = GetSql.getSqlInsertSubmissionLink(currentSubmissionId, id.getKey(), guildTag);
                statement.execute(insertSql);
            }
            statement.executeBatch();
            statement.close();
            currentSubmissionId++;
        }
    }

    public static int insertIncompleteSubmission(SubmissionData submissionData) throws SQLException {
        int id;
        synchronized (syncDB) {
            Statement statement = database.createStatement();
            String insertSql = GetSql.getSqlInsertResponse(currentResponseId, submissionData);
            statement.execute(insertSql);
            for (Pair<Long, String> idAndName : submissionData.getSubmittersNameAndIds()) {
                insertSql = GetSql.getSqlInsertResponseSubmitters(currentResponseId, idAndName.getKey(), idAndName.getValue());
                statement.execute(insertSql);
            }
            statement.close();
            id = currentResponseId;
            currentResponseId++;
        }
        return id;
    }

    public static void insertIncompleteSubmissionLink(long messageId, long channelId, int responseId) throws SQLException {
        synchronized (syncDB) {
            Statement statement = database.createStatement();
            String sql = GetSql.getSqlInsertResponseLink(messageId, channelId, responseId);
            statement.execute(sql);
            statement.close();
        }
    }

    public static void insertPlayer(Pair<Long, String> id, String guildTag, String guildName) throws SQLException {
        // no sync because this should be called while synced already
        try {
            SheetsPlayerStats.addProfile(id.getKey(), id.getValue());
        } catch (IOException ignored) { //this is fine because updates will add this player later somehow
        }
        if (guildName != null) {
            try {
                SheetsPlayerStats.updateGuild(guildName, guildTag, id.getKey(), id.getValue());
            } catch (IOException ignored) { //this is fine because updates will add this player later somehow
            }
        }
        Statement statement = VerifyDB.database.createStatement();
        statement.execute(GetSql.getSqlInsertPlayers(id, guildName, guildTag));
        statement.close();
    }
}
