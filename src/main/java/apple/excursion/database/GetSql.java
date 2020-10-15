package apple.excursion.database;

import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

class GetSql {
    private static final HashBiMap<Character, Character> incompatibleToCompatible = HashBiMap.create();

    static {
        incompatibleToCompatible.put('\'', '$');
    }

    // all the exists sql
    @NotNull
    static String getSqlExistsLbGuild(String guildTag, String monthName) {
        return String.format("SELECT COUNT(1) " +
                        "FROM %s " +
                        "WHERE guild_tag = '%s' " +
                        "LIMIT 1;",
                monthName, guildTag);
    }

    @NotNull
    public static String getSqlExistsGuild(String guildTag) {
        return String.format("SELECT COUNT(1) " +
                        "FROM guilds " +
                        "WHERE guild_tag = '%s' " +
                        "LIMIT 1;",
                guildTag);
    }

    @NotNull
    static String getSqlExistsLbPlayer(Pair<Long, String> id, String monthName) {
        return "SELECT COUNT(1)" +
                "FROM " + monthName + " "
                + "WHERE player_uid = '"
                + id.getKey()
                + "' LIMIT 1;";
    }

    @NotNull
    static String getSqlExistsPlayer(Long id) {
        return "SELECT COUNT(1)" +
                "FROM players " +
                "WHERE player_uid = '"
                + id
                + "' LIMIT 1;";
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
    static String getSqlInsertPlayers(Pair<Long, String> id, String guildName, String guildTag, int submissionId) {
        return "INSERT INTO players(player_uid, player_name, guild_name, guild_tag, submission_ids, score) "
                + "VALUES "
                + String.format("('%d','%s',%s,%s,'%s',0);",
                id.getKey(),
                id.getValue(),
                guildName == null ? null : String.format("'%s'", guildName),
                guildTag == null ? null : String.format("'%s'", guildTag),
                submissionId == -1 ? "" : String.valueOf(submissionId)
        );
    }

    @NotNull
    static String getSqlInsertSubmission(SubmissionData data) {
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
    public static String getSqlInsertGuild(String guildName, String guildTag) {
        return String.format("INSERT INTO guilds (guild_name, guild_tag, submissions) "
                        + "VALUES ('%s', '%s','%s');",
                guildName, guildTag, "");
    }

    @NotNull
    static String getSqlInsertLbPlayers(Pair<Long, String> id, String monthName, int taskScore, int count) {
        return String.format("INSERT INTO %s (player_uid, score, submissions_count) "
                        + "VALUES ('%d', %d, %d);",
                monthName, id.getKey(), taskScore, count
        );
    }

    @NotNull
    static String getSqlInsertLbGuild(String guildName, String guildTag, String monthName, int taskScore, int count) {
        return String.format("INSERT INTO %s (guild_tag, guild_name, score, submissions_count) "
                        + "VALUES ('%s', '%s', %d, %d);"
                , monthName, guildTag, guildName, taskScore, count);
    }


    // all the get sql


    @NotNull
    static String getSqlSubmissionGetAll(String submissionId) {
        return String.format("SELECT * " +
                "FROM submissions " +
                "WHERE id = %s;", submissionId);
    }

    @NotNull
    static String getSqlGetPlayerName(String id) {
        return String.format("SELECT player_name " +
                "FROM players " +
                "WHERE player_uid = '%s';", id);
    }

    @NotNull
    public static String getSqlGetGuilds() {
        return "SELECT * FROM guilds;";
    }

    @NotNull
    static String getSqlGetPlayerAll(long id) {
        return String.format("SELECT * " +
                "FROM players " +
                "WHERE player_uid = '%d';", id);
    }

    @NotNull
    static String getSqlGetLbGuild(String guildName, String guildTag, String monthName) {
        return String.format(
                "SELECT score, submissions_count "
                        + "FROM %s "
                        + "WHERE (guild_name = '%s' AND guild_tag = '%s') " +
                        "LIMIT 1;"
                , monthName, guildName, guildTag
        );
    }

    @NotNull
    public static String getSqlGetPlayersInGuild(String tag) {
        return String.format("SELECT * " +
                "FROM players " +
                "WHERE guild_tag = '%s'", tag);
    }

    @NotNull
    static String getSqlGetPlayerSubmissionIdsAndScore(Pair<Long, String> id) {
        return "SELECT submission_ids, score " +
                "FROM players " +
                "WHERE player_uid = '" + id.getKey() +
                "' LIMIT 1";
    }

    @NotNull
    static String getSqlGetPlayerGuild(Pair<Long, String> id) {
        return "SELECT guild_name, guild_tag " +
                "FROM players " +
                "WHERE player_uid = '" + id.getKey() +
                "' LIMIT 1;";
    }

    @NotNull
    static String getSqlGetLbPlayer(Pair<Long, String> id, String monthName) {
        return "SELECT score, submissions_count " +
                "FROM " + monthName + " "
                + "WHERE player_uid = '"
                + id.getKey()
                + "' LIMIT 1;";
    }


    // all the update sql


    @NotNull
    static String getSqlUpdatePlayerSubmissionIdsAndScore(Pair<Long, String> id, String submission_ids, int score) {
        return String.format("UPDATE players " +
                "SET submission_ids = '%s', score = %d " +
                "WHERE player_uid = '%s'", submission_ids, score, id.getKey());
    }

    @NotNull
    static String getSqlUpdateLbPlayer(Pair<Long, String> id, String monthName, int score, int count) {
        return String.format("UPDATE %s "
                        + "SET score = %d, submissions_count = %d "
                        + "WHERE player_uid = '%d';"
                , monthName, score, count, id.getKey());
    }

    @NotNull
    static String getSqlUpdateLbGuild(String guildName, String guildTag, String monthName, int score, int count) {
        return String.format("UPDATE %s "
                        + "SET score = %d, submissions_count = %d "
                        + "WHERE (guild_name = '%s' AND guild_tag = '%s');"
                , monthName, score, count, guildName, guildTag
        );
    }

    @NotNull
    public static String updatePlayerName(Long id, String playerName) {
        return String.format("UPDATE players " +
                        "SET player_name = %s " +
                        "WHERE player_uid = '%d'",
                playerName, id);
    }

    @NotNull
    public static String getSqlUpdatePlayerGuild(long id, String name, String tag) {
        return String.format("UPDATE players " +
                        "SET guild_name = '%s', guild_tag = '%s' " +
                        "WHERE player_uid = '%d'",
                name, tag, id);
    }

    @NotNull
    public static String getSqlUpdateGuildSubmissions(String guildTag, String submissions) {
        return String.format("UPDATE guilds " +
                "SET submissions = '%s' " +
                "WHERE guild_tag = '%s'", submissions, guildTag);
    }

    @NotNull
    public static String getSqlGetGuildSubmissions(String guildTag) {
        return String.format("SELECT submissions " +
                "FROM guilds " +
                "WHERE guild_tag = '%s'", guildTag);
    }


    // helper methods
    @NotNull
    public static String convertTaskNameToSql(@NotNull String taskName) {
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

}
