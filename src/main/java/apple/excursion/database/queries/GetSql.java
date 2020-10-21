package apple.excursion.database.queries;

import apple.excursion.database.VerifyDB;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class GetSql {
    private static final HashBiMap<Character, Character> incompatibleToCompatible = HashBiMap.create();

    static {
        incompatibleToCompatible.put('\'', '$');
    }

    // all the exists sql

    @NotNull
    static String getSqlExistsPlayer(Long id) {
        return "SELECT COUNT(1)" +
                "FROM players " +
                "WHERE player_uid = "
                + id
                + " LIMIT 1;";
    }


    // all the insert sql


    @NotNull
    public static String getSqlInsertDailyTask(String monthName, int dayOfMonth, Collection<Task> todayTasks) {
        return String.format("INSERT INTO %s (date, task_names) " +
                        "VALUES (%d, '%s'); ",
                monthName,
                dayOfMonth,
                todayTasks.stream().map(task -> convertTaskNameToSql(task.taskName)).collect(Collectors.joining(","))
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
    static String getSqlInsertSubmission(SubmissionData data) {
        final Collection<String> linksList = data.getLinks();
        String links;
        if (linksList.isEmpty())
            links = null;
        else
            links = "'" + String.join(",", linksList) + "'";

        return String.format("INSERT INTO submissions "
                        + "VALUES "
                        + "(%d,%d,'%s',%s,%d,'%s',%d);",
                VerifyDB.currentSubmissionId,
                data.getTimeEpoch() * 1000,
                convertTaskNameToSql(data.getTaskName()),
                links,
                data.getSubmitterId(),
                data.getType().name(),
                data.getTaskScore()
        );
    }

    @NotNull
    static String getSqlInsertSubmission(long playerId, int score, String taskName) {
        return String.format("INSERT INTO submissions "
                        + "VALUES "
                        + "(%d,%d,'%s',%s,%d,'%s',%d);",
                VerifyDB.currentSubmissionId,
                System.currentTimeMillis(),
                convertTaskNameToSql(taskName),
                null,
                playerId,
                SyncDB.SYNC_TASK_TYPE,
                score
        );
    }

    @NotNull
    static String getSqlInsertGuild(String guildName, String guildTag) {
        return String.format("INSERT INTO guilds "
                        + "VALUES ('%s', '%s');",
                guildTag, guildName);
    }


    // all the get sql
    @NotNull
    static String getSqlGetPlayerSubmissionHistory(long id, int submissionsToGet) {
        return String.format("SELECT group_concat(players.player_name),\n" +
                "       data.submitter_name,\n" +
                "       data.date_submitted,\n" +
                "       data.task_name,\n" +
                "       data.links,\n" +
                "       data.submission_type,\n" +
                "       data.score\n" +
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
    static String getSqlGetCalendar(String monthYear) {
        return "SELECT * FROM " + monthYear;
    }


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
    static String getSqlGetGuilds(long start, long end) {
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
                "         WHERE date_submitted BETWEEN " + start + " AND " + end + "\n" +
                "         GROUP BY players.player_uid\n" +
                "     ) as player_score\n" +
                "GROUP BY player_score.guild_tag;";
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
    static String getSqlInsertSubmissionLink(int submissionId, long playerId, String guildTag) {
        return String.format("INSERT INTO submissions_link VALUES (%d,%d,'%s')", submissionId, playerId, guildTag);
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
                "       data.score\n" +
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
                "       data.score\n" +
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
    static String getSqlUpdatePlayerSoulJuice(long id, int juiceToAddToDatabase) {
        return String.format("UPDATE players\n" +
                "SET soul_juice = soul_juice + %d\n" +
                "WHERE player_uid = %d;", juiceToAddToDatabase, id);
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

}
