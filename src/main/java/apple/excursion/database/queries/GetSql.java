package apple.excursion.database.queries;

import apple.excursion.database.VerifyDB;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.messages.benchmark.CalendarMessage;
import apple.excursion.utils.Pair;
import com.google.common.collect.HashBiMap;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GetSql {
    private static final HashBiMap<Character, Character> incompatibleToCompatible = HashBiMap.create();

    static {
        incompatibleToCompatible.put('\'', '$');
    }

    // all the exists sql

    @NotNull
    static String getSqlExistsPlayer(Long id) {
        return "SELECT COUNT(1) " +
                "FROM players " +
                "WHERE player_uid = "
                + id
                + " LIMIT 1;";
    }

    public static String getSqlExistsCrossChat(long serverId) {
        return String.format("SELECT COUNT(1) FROM cross_chat WHERE server_id = %d LIMIT 1;", serverId);
    }

    // all the insert sql

    @NotNull
    public static String getSqlInsertDailyTask(String monthName, int dayOfMonth, Collection<Task> todayTasks) {
        return String.format("INSERT INTO %s (date, task_names) " +
                        "VALUES (%d, '%s'); ",
                monthName,
                dayOfMonth,
                todayTasks.stream().map(task -> convertTaskNameToSql(task.name)).collect(Collectors.joining(","))
        );
    }

    @NotNull
    static String getSqlInsertPlayers(Pair<Long, String> id, String guildName, String guildTag) {
        if (guildName == null) {
            return "INSERT INTO players (player_uid, player_name) "
                    + "VALUES "
                    + String.format("('%d','%s');",
                    id.getKey(),
                    id.getValue()
            );
        } else {
            return "INSERT INTO players "
                    + "VALUES "
                    + String.format("('%d','%s','%s','%s',0);",
                    id.getKey(),
                    id.getValue(),
                    guildName,
                    guildTag
            );
        }
    }

    @NotNull
    static String getSqlInsertSubmissionLink(int submissionId, long playerId, String guildTag) {
        return String.format("INSERT INTO submissions_link VALUES (%d,%d,'%s')", submissionId, playerId, guildTag);
    }

    @NotNull
    static String getSqlInsertSubmission(SubmissionData data, int currentSubmissionId) {
        final Collection<String> linksList = data.getLinks();
        String links;
        if (linksList.isEmpty())
            links = null;
        else
            links = "'" + String.join(",", linksList) + "'";

        return String.format("INSERT INTO submissions "
                        + "VALUES "
                        + "(%d,%d,'%s',%s,%d,'%s',%d, %s);",
                currentSubmissionId,
                data.getTimeEpoch(),
                convertTaskNameToSql(data.getTaskName()),
                links,
                data.getSubmitterId(),
                data.getType().name(),
                data.getTaskScore(),
                data.getImageUrl() == null ? null : String.format("'%s'", data.getImageUrl())
        );
    }

    @NotNull
    static String getSqlInsertSubmission(long playerId, int score, String taskName) {
        return String.format("INSERT INTO submissions "
                        + "VALUES "
                        + "(%d,%d,'%s',%s,%d,'%s',%d,%s);",
                VerifyDB.currentSubmissionId,
                CalendarMessage.EPOCH_BEFORE_START_OF_SUBMISSION_HISTORY,
                convertTaskNameToSql(taskName),
                null,
                playerId,
                SyncDB.SYNC_TASK_TYPE,
                score,
                null
        );
    }

    @NotNull
    static String getSqlInsertGuild(String guildName, String guildTag) {
        return String.format("INSERT INTO guilds "
                        + "VALUES ('%s', '%s');",
                guildTag, guildName);
    }


    // all the get sql

    // all the get guilds
    @NotNull
    static String getSqlGetGuilds() {
        return "SELECT sum(player_score.score) AS guild_score, player_score.guild_tag, player_score.guild_name, player_score.player_name, player_score.player_uid, max(player_score.score)\n" +
                "FROM (\n" +
                "         SELECT players.player_name, players.player_uid, sum(submissions.score) as score, guilds.guild_tag, guilds.guild_name\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link\n" +
                "                             ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions\n" +
                "                             ON submissions_link.submission_id = submissions.id\n" +
                "                  INNER JOIN guilds\n" +
                "                             ON guilds.guild_tag = submissions_link.guild_tag\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) as player_score\n" +
                "GROUP BY player_score.guild_tag;";
    }

    @NotNull
    static String getSqlGetGuildNames() {
        return "SELECT guild_tag, guild_name FROM guilds;";
    }

    @NotNull
    public static String getSqlGetGuildSubmissionHistory(String guildTag, long startTime, long endTime) {
        return "SELECT group_concat(players.player_name),\n" +
                "       data.submitter_name,\n" +
                "       data.date_submitted,\n" +
                "       data.task_name,\n" +
                "       data.links,\n" +
                "       data.submission_type,\n" +
                "       data.score,\n" +
                "       data.image\n" +
                "FROM (\n" +
                "         SELECT s.*, players.player_name as submitter_name\n" +
                "         FROM (\n" +
                "                  SELECT submissions.*\n" +
                "                  FROM submissions_link\n" +
                "                           INNER JOIN submissions\n" +
                "                                      ON submissions_link.submission_id = submissions.id\n" +
                "                  WHERE submissions_link.guild_tag = '" + guildTag + "'\n" +
                "                  AND submissions.date_submitted BETWEEN " + startTime + " AND " + endTime + "\n" +
                "                  ORDER BY submissions.date_submitted DESC\n" +
                "              ) as s\n" +
                "                  INNER JOIN players\n" +
                "                             ON s.submitter = players.player_uid\n" +
                "     ) AS data\n" +
                "         INNER JOIN submissions_link\n" +
                "                    ON submissions_link.submission_id = data.id\n" +
                "         INNER JOIN players\n" +
                "                    ON submissions_link.player_id = players.player_uid\n" +
                "GROUP BY data.id;";
    }

    @NotNull
    static String getSqlGetGuildSubmissionHistory(String guildTag) {
        return "SELECT group_concat(players.player_name),\n" +
                "       data.submitter_name,\n" +
                "       data.date_submitted,\n" +
                "       data.task_name,\n" +
                "       data.links,\n" +
                "       data.submission_type,\n" +
                "       data.score,\n" +
                "       data.image\n" +
                "FROM (\n" +
                "         SELECT s.*, players.player_name as submitter_name\n" +
                "         FROM (\n" +
                "                  SELECT submissions.*\n" +
                "                  FROM submissions_link\n" +
                "                           INNER JOIN submissions\n" +
                "                                      ON submissions_link.submission_id = submissions.id\n" +
                "                  WHERE submissions_link.guild_tag = '" + guildTag + "'\n" +
                "                  ORDER BY submissions.date_submitted DESC\n" +
                "              ) as s\n" +
                "                  INNER JOIN players\n" +
                "                             ON s.submitter = players.player_uid\n" +
                "     ) AS data\n" +
                "         INNER JOIN submissions_link\n" +
                "                    ON submissions_link.submission_id = data.id\n" +
                "         INNER JOIN players\n" +
                "                    ON submissions_link.player_id = players.player_uid\n" +
                "GROUP BY data.id;";
    }

    @NotNull
    static String getSqlGetGuilds(long start, long end) {
        return "SELECT sum(player_score.score) AS guild_score, player_score.guild_tag, player_score.guild_name, player_score.player_uid, player_score.player_name, max(player_score.score)\n" +
                "FROM (\n" +
                "         SELECT players.player_name, players.player_uid, sum(submissions.score) as score, guilds.guild_tag, guilds.guild_name\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link\n" +
                "                             ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions\n" +
                "                             ON submissions_link.submission_id = submissions.id\n" +
                "                  INNER JOIN guilds\n" +
                "                             ON guilds.guild_tag = submissions_link.guild_tag\n" +
                "         WHERE date_submitted BETWEEN " + start + " AND " + end + "\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) as player_score\n" +
                "GROUP BY player_score.guild_tag;";
    }

    @NotNull
    static String getSqlGetPlayersInGuild(String tag) {
        return String.format("SELECT data.player_uid, data.player_name,guilds.guild_tag,guilds.guild_name,data.score,data.soul_juice\n" +
                "FROM (\n" +
                "         SELECT players.player_uid, players.player_name, players.guild_tag, sum(submissions.score) as score, players.soul_juice\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link\n" +
                "                             ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions\n" +
                "                             ON submissions_link.submission_id = submissions.id\n" +
                "         WHERE submissions_link.guild_tag = '%s'\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) AS data\n" +
                "INNER JOIN guilds\n" +
                "ON guilds.guild_tag = data.guild_tag\n" +
                "ORDER BY data.score DESC;", tag);
    }

    @NotNull
    public static String getSqlGetPlayersInGuild(String tag, long startTime, long endTime) {
        return String.format("SELECT data.player_uid, data.player_name,guilds.guild_tag,guilds.guild_name,data.score,data.soul_juice\n" +
                "FROM (\n" +
                "         SELECT players.player_uid, players.player_name, players.guild_tag, sum(submissions.score) as score, players.soul_juice\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link\n" +
                "                             ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions\n" +
                "                             ON submissions_link.submission_id = submissions.id\n" +
                "         WHERE submissions_link.guild_tag = '%s'\n" +
                "         AND date_submitted BETWEEN %d AND %d\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) AS data\n" +
                "INNER JOIN guilds\n" +
                "ON guilds.guild_tag = data.guild_tag\n" +
                "ORDER BY data.score DESC;", tag, startTime, endTime);
    }

    @NotNull
    static String getSqlGetPlayerGuild(long id) {
        return "SELECT guild_name, guild_tag " +
                "FROM players " +
                "WHERE player_uid = " + id +
                " LIMIT 1;";
    }

    // all the get player
    @NotNull
    static String getSqlGetPlayerSubmissionHistory(long id, int submissionsToGet) {
        return String.format("SELECT group_concat(players.player_name),\n" +
                "       data.submitter_name,\n" +
                "       data.date_submitted,\n" +
                "       data.task_name,\n" +
                "       data.links,\n" +
                "       data.submission_type,\n" +
                "       data.score,\n" +
                "       data.image\n" +
                "FROM (\n" +
                "         SELECT s.*, players.player_name as submitter_name\n" +
                "         FROM (\n" +
                "                  SELECT submissions.*\n" +
                "                  FROM submissions_link\n" +
                "                           INNER JOIN submissions\n" +
                "                                      ON submissions_link.submission_id = submissions.id\n" +
                "                  WHERE submissions_link.player_id = %d\n" +
                "                  ORDER BY submissions.date_submitted DESC\n" +
                (submissionsToGet == -1 ? "" : String.format("LIMIT %d ", submissionsToGet)) +
                "              ) as s\n" +
                "                  INNER JOIN players\n" +
                "                             ON s.submitter = players.player_uid\n" +
                "     ) AS data\n" +
                "         INNER JOIN submissions_link\n" +
                "                    ON submissions_link.submission_id = data.id\n" +
                "         INNER JOIN players\n" +
                "                    ON submissions_link.player_id = players.player_uid\n" +
                "GROUP BY data.id", id);
    }

    @NotNull
    static String getSqlGetPlayerLeaderboard() {
        return "SELECT players.player_name, players.guild_tag, players.guild_name, playerData.*\n" +
                "FROM (\n" +
                "         SELECT sum(submissions.score) AS score, player_uid\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions ON submissions_link.submission_id = submissions.id\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) AS playerData\n" +
                "         INNER JOIN players\n" +
                "                    ON playerData.player_uid = players.player_uid";
    }

    @NotNull
    static String getSqlGetPlayerHeaders() {
        return "SELECT players.player_uid, players.player_name, players.soul_juice, playerData.score, players.guild_name, players.guild_tag\n" +
                "FROM (\n" +
                "         SELECT sum(submissions.score) AS score, player_uid\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions ON submissions_link.submission_id = submissions.id\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) AS playerData\n" +
                "         INNER JOIN players\n" +
                "                    ON playerData.player_uid = players.player_uid";
    }

    @NotNull
    static String getSqlGetPlayerHeadersNoScore() {
        return "SELECT players.player_uid, players.player_name, players.soul_juice, players.guild_name, players.guild_tag FROM players";
    }

    @NotNull
    public static String getSqlGetPlayerLeaderboard(long startTime, long endTime) {
        return "SELECT players.player_name, players.guild_tag, players.guild_name, playerData.*\n" +
                "FROM (\n" +
                "         SELECT sum(submissions.score) AS score, player_uid\n" +
                "         FROM players\n" +
                "                  INNER JOIN submissions_link ON players.player_uid = submissions_link.player_id\n" +
                "                  INNER JOIN submissions ON submissions_link.submission_id = submissions.id\n" +
                "         WHERE date_submitted BETWEEN " + startTime + " AND " + endTime + "\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) AS playerData\n" +
                "         INNER JOIN players\n" +
                "                    ON playerData.player_uid = players.player_uid";
    }

    @NotNull
    static String getSqlGetPlayerAll(long id) {
        return "SELECT sum(submissions.score),\n" +
                "       players.player_name,\n" +
                "       players.guild_tag,\n" +
                "       players.guild_name,\n" +
                "       players.soul_juice\n" +
                "FROM players\n" +
                "         INNER JOIN submissions_link ON players.player_uid = submissions_link.player_id\n" +
                "         INNER JOIN submissions ON submissions_link.submission_id = submissions.id\n" +
                "WHERE players.player_uid = " + id + ";";
    }

    // all the get calendar
    @NotNull
    static String getSqlGetCalendar(String monthYear) {
        return "SELECT * FROM " + monthYear;
    }


    // all the update sql
    @NotNull
    static String getSqlUpdatePlayerSoulJuice(long id, int juiceToAddToDatabase) {
        return String.format("UPDATE players\n" +
                "SET soul_juice = soul_juice + %d\n" +
                "WHERE player_uid = %d;", juiceToAddToDatabase, id);
    }

    @NotNull
    static String updatePlayerName(Long id, String playerName) {
        return String.format("UPDATE players " +
                        "SET player_name = '%s' " +
                        "WHERE player_uid = '%d'",
                playerName, id);
    }

    @NotNull
    static String getSqlUpdatePlayerGuild(long id, String name, String tag) {
        return String.format("UPDATE players " +
                        "SET guild_name = '%s', guild_tag = '%s' " +
                        "WHERE player_uid = '%d'",
                name, tag, id);
    }


    // helper methods
    @NotNull
    private static String convertTaskNameToSql(@NotNull String taskName) {
        char[] chars = taskName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Character newC;
            if ((newC = incompatibleToCompatible.get(c)) != null) {
                chars[i] = newC;
            }
        }
        return new String(chars);
    }

    @NotNull
    public static String convertTaskNameFromSql(@NotNull String taskName) {
        char[] chars = taskName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            Character newC;
            if ((newC = incompatibleToCompatible.inverse().get(c)) != null) {
                chars[i] = newC;
            }
        }
        return new String(chars);
    }


    @NotNull
    public static String getSqlInsertResponse(int currentResponseId, SubmissionData submissionData) {
        return String.format("INSERT INTO response " +
                        "VALUES (%d,0,0,%s,%d,%s,%s,'%s','%s',%d,'%s','%s',%d);",
                currentResponseId,
                null,
                submissionData.getTimeEpoch(),
                submissionData.getAttachment() == null ? null : "'" + submissionData.getAttachment() + "'",
                submissionData.getLinks() == null ? null : "'" + String.join("`", submissionData.getLinks()) + "'",
                submissionData.getCategory(),
                convertTaskNameToSql(submissionData.getTaskName()),
                submissionData.getTaskScore(),
                submissionData.getType().name(),
                submissionData.getSubmitterName(),
                submissionData.getSubmitterId()
        );
    }

    public static String getSqlInsertResponseSubmitters(int currentResponseId, long submitterId, String name) {
        return String.format("INSERT INTO response_submitters " +
                        "VALUES (%d,%d,'%s');",
                currentResponseId, submitterId, name
        );
    }

    public static String getSqlInsertResponseLink(long messageId, long channelId, int responseId) {
        return String.format("INSERT INTO response_link " +
                "VALUES (%d,%d,%d);", messageId, channelId, responseId);
    }

    public static String getSqlGetResponseSubmissionData(long channelId, long messageId) {
        return String.format("SELECT response.*\n" +
                "FROM response_link\n" +
                "         INNER JOIN response ON response.response_id = response_link.response_id\n" +
                "WHERE channel_id = %d\n" +
                "  AND message_id = %d;", channelId, messageId);
    }

    public static String getSqlGetResponseSubmissionNames(int responseId) {
        return String.format("SELECT response_submitters.submitter_id, response_submitters.submitter_name\n" +
                "FROM response_submitters\n" +
                "WHERE response_id = %d;", responseId);
    }

    public static String getSqlGetResponseReviewerMessages(int responseId) {
        return String.format("SELECT channel_id,message_id\n" +
                "FROM response_link\n" +
                "WHERE response_id = %d;", responseId);
    }

    public static String getSqlUpdateResponseStatus(boolean isAccepted, boolean isCompleted, int responseId) {
        return String.format("UPDATE response\n" +
                "SET is_accepted = %b, is_completed = %b\n" +
                "WHERE response_id = %d;", isAccepted, isCompleted, responseId);
    }

    public static String getSqlGetCrossChat() {
        return "SELECT * FROM cross_chat;";
    }

    public static String getSqlInsertCrossChat(long serverId, long channelId) {
        return String.format("INSERT INTO cross_chat VALUES (%d,%d);", serverId, channelId);
    }

    public static String getSqlRemoveCrossChat(long serverId) {
        return "DELETE FROM cross_chat WHERE server_id = " + serverId;
    }

    public static String getSqlUpdateCrossChat(long serverId, long channelId) {
        return String.format("\n" +
                "UPDATE cross_chat\n" +
                "SET channel_id = %d\n" +
                "WHERE server_id = %d;", serverId, channelId);
    }

    public static String getSqlGetCrossChatMessages(long serverId, long channelId, long messageId) {
        return String.format("SELECT myMessageId,discordServerId, discordChannelId, discordMessageId\n" +
                "FROM cross_chat_messages\n" +
                "WHERE cross_chat_messages.myMessageId = (\n" +
                "    SELECT myMessageId\n" +
                "    FROM cross_chat_messages\n" +
                "    WHERE cross_chat_messages.discordMessageId = %d\n" +
                "      AND cross_chat_messages.discordChannelId = %d\n" +
                "      AND cross_chat_messages.discordServerId = %d\n" +
                ");", messageId, channelId, serverId);
    }

    public static String getSqlInsertCrossChatSent(long currentMyMessageId, long owner, String username, int color, String avatarUrl, String imageUrl, String description) {
        return String.format("INSERT INTO cross_chat_message_sent \n" +
                "VALUES (%d,%d,'%s',%d,'%s',%s,'%s','%s');", currentMyMessageId, owner, convertTaskNameToSql(username), color, avatarUrl, imageUrl == null ? null : String.format("'%s'", imageUrl), convertTaskNameToSql(description), "");
    }

    public static String getSqlInsertCrossChatMessages(long currentMyMessageId, long serverId, long channelId, long messageId) {
        return String.format("INSERT INTO cross_chat_messages \n" +
                "VALUES (%d,%d,%d,%d);", currentMyMessageId, serverId, channelId, messageId);
    }

    public static String getSqlGetCrossChatMessageContent(long myMessageId) {
        return String.format("SELECT * FROM cross_chat_message_sent WHERE myMessageId = %d;", myMessageId);
    }

    public static String getSqlUpdateCrossChatReactions(long myMessageId, MessageReactionAddEvent event) {
        Member member = event.getMember();
        String username = member == null ? event.getUser() == null ? "???" : event.getUser().getName() : member.getEffectiveName();
        return String.format("UPDATE cross_chat_message_sent\n" +
                "SET reactions = cross_chat_message_sent.reactions || '%s'\n" +
                "WHERE myMessageId = %d;", String.format(",%s.%s", username, event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getEmote().getName()), myMessageId);
    }

    public static String getSqlGetCrossChatMessages(long myMessageId, long owner) {
        return String.format("SELECT *\n" +
                "FROM (\n" +
                "         SELECT *\n" +
                "         FROM cross_chat_messages\n" +
                "         WHERE myMessageId = %d\n" +
                "     ) as matches\n" +
                "         INNER JOIN cross_chat_message_sent\n" +
                "                    ON matches.myMessageId = cross_chat_message_sent.myMessageId\n" +
                "WHERE cross_chat_message_sent.owner = %d", myMessageId, owner);
    }

    public static String getSqlUpdateCalendar(String monthName, int day, List<String> newTasksToday) {
        newTasksToday = newTasksToday.stream().map(GetSql::convertTaskNameToSql).collect(Collectors.toList());
        return String.format("UPDATE %s \n" +
                "SET task_names = '%s'\n" +
                "WHERE date = %d;", monthName, String.join(",", newTasksToday), day);
    }

    public static String getSqlUpdateCrossChatDescription(long messageId, String description) {
        return String.format("UPDATE cross_chat_message_sent\n" +
                "SET description = '%s'\n" +
                "WHERE myMessageId  =  %d;", convertTaskNameToSql(description), messageId);
    }

    public static String getSqlUpdateResponseSubmissionId(int responseId, int submissionId) {
        return String.format("UPDATE response\n" +
                "SET submission_id = %d\n" +
                "WHERE response_id = %d;", submissionId, responseId);
    }

    public static String dropSubmission(int submissionId) {
        return String.format("DELETE\n" +
                "FROM submissions\n" +
                "WHERE id = %d;", submissionId);
    }

    public static String dropSubmissionLink(int submissionId) {
        return String.format("DELETE\n" +
                "FROM submissions_link\n" +
                "WHERE submission_id = %d\n", submissionId);
    }

    public static String getSqlUpdateTaskNameSubmissions(String oldName, String newName) {
        return String.format("update submissions\n" +
                "set task_name = '%s'\n" +
                "where task_name = '%s';\n", newName, oldName);
    }

    public static String getSqlUpdateTaskNameResponses(String oldName, String newName) {
        return String.format("update response\n" +
                "set task_name = '%s'\n" +
                "where task_name = '%s';\n", newName, oldName);
    }

    public static String getSqlGetUncompletedResponses(long channelId) {
        return String.format("SELECT channel_id, message_id\n" +
                "FROM response_link\n" +
                "         INNER JOIN response ON response.response_id = response_link.response_id\n" +
                "WHERE response.is_completed = FALSE\n" +
                "  AND channel_id = %d;", channelId);
    }
}
