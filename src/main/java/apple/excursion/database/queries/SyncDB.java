package apple.excursion.database.queries;

import apple.excursion.ExcursionMain;
import apple.excursion.database.VerifyDB;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.TaskSimpleCompleted;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.messages.benchmark.CalendarMessage;
import apple.excursion.sheets.SheetsConstants;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.sheets.profiles.Profile;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.Pair;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

public class SyncDB {
    public static final String SYNC_TASK_TYPE = "SYNC";
    private static final String SYNC_TASK_NAME = "SYNC_TASK_NAME";

    public static List<String> sync(Set<Profile> sheetData, List<PlayerHeader> databasePlayers, List<PlayerData> players, List<GuildHeader> databaseGuilds) throws SQLException {
        List<String> logs = new ArrayList<>();
        synchronized (VerifyDB.syncDB) {
            Statement statement = VerifyDB.database.createStatement();
            String sql;
            for (Profile sheetPlayer : sheetData) {
                PlayerHeader databasePlayer = null;
                for (PlayerHeader header : databasePlayers) {
                    if (header.id == sheetPlayer.getId()) {
                        databasePlayer = header;
                        break;
                    }
                }
                PlayerData playerData = null;
                for (PlayerData data : players) {
                    if (data.id == sheetPlayer.getId()) {
                        playerData = data;
                        break;
                    }
                }
                int score;
                int soulJuice;
                String guildName = sheetPlayer.getGuild();
                String guildTag = sheetPlayer.getGuildTag();
                if (!guildName.isBlank() && !guildTag.isBlank()) {
                    GuildHeader guild = null;
                    for (GuildHeader guildHeader : databaseGuilds) {
                        if (guildHeader.tag.equals(guildTag)) {
                            guild = guildHeader;
                            break;
                        }
                    }
                    if (guild == null) {
                        // insert the guild into the DB
                        sql = GetSql.getSqlInsertGuild(guildName, guildTag);
                        databaseGuilds.add(new GuildHeader(guildTag, guildName));
                        statement.addBatch(sql);
                        logs.add(String.format("Added guild %s [%s]", guildName, guildTag));
                    }
                }

                if (databasePlayer == null) {
                    // insert the player into the database
                    sql = GetSql.getSqlInsertPlayers(
                            new Pair<>(sheetPlayer.getId(), sheetPlayer.getName()),
                            guildName.isBlank() ? null : guildName,
                            guildTag.isBlank() ? null : guildTag);
                    statement.addBatch(sql);
                    score = 0;
                    soulJuice = 0;
                    logs.add(String.format("Added player <%d,%s> in %s [%s]",
                            sheetPlayer.getId(),
                            sheetPlayer.getName(),
                            guildName,
                            guildTag));
                } else {
                    score = databasePlayer.score;
                    soulJuice = databasePlayer.soulJuice;
                }
                if (playerData == null)
                    playerData = new PlayerData(sheetPlayer.getId(), sheetPlayer.getName(), guildName, guildTag, Collections.emptyList(), 0, 0);
                int pointsToAddToDatabase = sheetPlayer.getTotalEp() - score;
                int juiceToAddToDatabase = sheetPlayer.getSoulJuice() - soulJuice;
                if (juiceToAddToDatabase != 0) {
                    sql = GetSql.getSqlUpdatePlayerSoulJuice(sheetPlayer.getId(), juiceToAddToDatabase);
                    statement.addBatch(sql);
                    logs.add(String.format("Added %d SoulJuice to <%d,%s>",
                            juiceToAddToDatabase,
                            sheetPlayer.getId(),
                            sheetPlayer.getName()));
                }
                // if we need to update the playerScore
                if (pointsToAddToDatabase != 0) {
                    for (TaskSimpleCompleted task : sheetPlayer.tasksDone) {
                        int pointsLeftToEarn = task.pointsEarned - playerData.getScoreOfSubmissionsWithName(task.name);
                        while (pointsLeftToEarn - task.points >= 0) {
                            sql = GetSql.getSqlInsertSubmission(sheetPlayer.getId(), task.points, task.name);
                            statement.addBatch(sql);
                            sql = GetSql.getSqlInsertSubmissionLink(VerifyDB.currentSubmissionId,
                                    sheetPlayer.getId(),
                                    guildTag.isBlank() ? VerifyDB.DEFAULT_GUILD_TAG : guildTag);
                            statement.addBatch(sql);
                            logs.add(String.format("Added submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
                                    task.name,
                                    VerifyDB.currentSubmissionId,
                                    task.points,
                                    sheetPlayer.getId(),
                                    sheetPlayer.getName(),
                                    guildName,
                                    guildTag));
                            VerifyDB.currentSubmissionId++;
                            pointsToAddToDatabase -= task.points;
                            pointsLeftToEarn -= task.points;
                        }
                        if (pointsLeftToEarn != 0) {
                            sql = GetSql.getSqlInsertSubmission(sheetPlayer.getId(), pointsLeftToEarn, task.name);
                            statement.addBatch(sql);
                            sql = GetSql.getSqlInsertSubmissionLink(VerifyDB.currentSubmissionId,
                                    sheetPlayer.getId(),
                                    guildTag.isBlank() ? VerifyDB.DEFAULT_GUILD_TAG : guildTag);
                            statement.addBatch(sql);
                            if (pointsLeftToEarn < 0)
                                logs.add(String.format("Subtracted submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
                                        task.name,
                                        VerifyDB.currentSubmissionId,
                                        pointsLeftToEarn,
                                        sheetPlayer.getId(),
                                        sheetPlayer.getName(),
                                        guildName,
                                        guildTag));
                            else
                                logs.add(String.format("Added submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
                                        task.name,
                                        VerifyDB.currentSubmissionId,
                                        pointsLeftToEarn,
                                        sheetPlayer.getId(),
                                        sheetPlayer.getName(),
                                        guildName,
                                        guildTag));
                            VerifyDB.currentSubmissionId++;
                            pointsToAddToDatabase -= pointsLeftToEarn;
                        }
                    }

                    if (pointsToAddToDatabase != 0) {
                        sql = GetSql.getSqlInsertSubmission(sheetPlayer.getId(), pointsToAddToDatabase, SYNC_TASK_NAME);
                        statement.addBatch(sql);
                        sql = GetSql.getSqlInsertSubmissionLink(VerifyDB.currentSubmissionId,
                                sheetPlayer.getId(),
                                guildTag.isBlank() ? VerifyDB.DEFAULT_GUILD_TAG : guildTag);
                        statement.addBatch(sql);
                        if (pointsToAddToDatabase > 0)
                            logs.add(String.format("Added submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
                                    SYNC_TASK_NAME,
                                    VerifyDB.currentSubmissionId,
                                    pointsToAddToDatabase,
                                    sheetPlayer.getId(),
                                    sheetPlayer.getName(),
                                    guildName,
                                    guildTag));
                        else
                            logs.add(String.format("Subtracted submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
                                    SYNC_TASK_NAME,
                                    VerifyDB.currentSubmissionId,
                                    pointsToAddToDatabase,
                                    sheetPlayer.getId(),
                                    sheetPlayer.getName(),
                                    guildName,
                                    guildTag));
                        VerifyDB.currentSubmissionId++;
                    }
                }
            }
            for (int i = 0; i < logs.size(); i++) {
                logs.set(i, logs.get(i).replaceAll("~", ""));
            }
            statement.executeBatch();
            statement.close();
        }
        return logs;
    }

    public static List<String> sync(List<SubmissionData> submissions, Set<Profile> profiles, List<Task> allTasks) throws SQLException {
        List<String> logs = new ArrayList<>();

        Map<Long, ProfileWithSubmission> idToPlayer = new HashMap<>();
        for (SubmissionData submission : submissions) {
            Profile profile = null;
            for (Profile p : profiles) {
                if (p.getId() == submission.getSubmitterId()) {
                    profile = p;
                    break;
                }
            }
            if (profile == null) continue;
            idToPlayer.putIfAbsent(submission.getSubmitterId(), new ProfileWithSubmission(profile));
            idToPlayer.get(submission.getSubmitterId()).addSubmission(submission);
        }
        for (ProfileWithSubmission player : idToPlayer.values()) {
            player.sortSubmissions(allTasks);
        }


        synchronized (VerifyDB.syncDB) {
            Set<Long> playerIds = new HashSet<>();
            for (PlayerHeader playerHeader : GetDB.getPlayerHeaders()) {
                playerIds.add(playerHeader.id);
            }
            Set<String> guildTags = new HashSet<>();
            for (GuildHeader guildHeader : GetDB.getGuildNameList()) {
                guildTags.add(guildHeader.tag);
            }
            Statement statement = VerifyDB.database.createStatement();
            for (ProfileWithSubmission playerSubmissions : idToPlayer.values()) {
                for (SubmissionData submission : playerSubmissions.submissions) {
                    if (playerIds.add(submission.getSubmitterId())) {
                        InsertDB.insertPlayer(
                                new Pair<>(submission.getSubmitterId(), submission.getSubmitterName()),
                                playerSubmissions.guildTag,
                                playerSubmissions.guildName
                        );
                    }
                    if (guildTags.add(playerSubmissions.guildTag)) {
                        statement.addBatch(GetSql.getSqlInsertGuild(playerSubmissions.guildName, playerSubmissions.guildTag));
                    }
                    statement.addBatch(GetSql.getSqlInsertSubmission(submission, VerifyDB.currentSubmissionId));

                    String guildTag = playerSubmissions.guildTag.isBlank() ? VerifyDB.DEFAULT_GUILD_TAG : playerSubmissions.guildTag;

                    logs.add(String.format("Added submission %s,%d of %d EP for <%d,%s> in %s [%s]",
                            submission.getTaskName(),
                            VerifyDB.currentSubmissionId,
                            submission.getTaskScore(),
                            submission.getSubmitterId(),
                            submission.getSubmitterName(),
                            playerSubmissions.guildName.isBlank() ? VerifyDB.DEFAULT_GUILD_NAME : playerSubmissions.guildName,
                            guildTag
                    ));
                    statement.addBatch(GetSql.getSqlInsertSubmissionLink(
                            VerifyDB.currentSubmissionId,
                            submission.getSubmitterId(),
                            guildTag
                    ));
                    VerifyDB.currentSubmissionId++;
                }
            }
            statement.executeBatch();
            statement.close();
        }
        return logs;
    }

    public static List<String> syncUsingDB(Set<Profile> sheetData, List<PlayerHeader> databasePlayers, List<PlayerData> databasePlayerDatas, List<TaskSimple> sheetTasks) throws IOException {
        List<String> logs = new ArrayList<>();
        List<Request> sheetRequests = new ArrayList<>();
        for (PlayerData databasePlayer : databasePlayerDatas) {
            Profile sheetPlayer = null;
            for (Profile profile : sheetData) {
                if (profile.hasId(databasePlayer.id)) {
                    sheetPlayer = profile;
                    break;
                }
            }
            if (sheetPlayer == null) {
                // todo make a new player row in the google sheet
                continue;
            }
            Map<String, List<OldSubmission>> databaseSubmissions = new HashMap<>();
            for (OldSubmission submission : databasePlayer.submissions) {
                databaseSubmissions.putIfAbsent(submission.taskName, new ArrayList<>());
                databaseSubmissions.get(submission.taskName).add(submission);
            }
            for (TaskSimpleCompleted sheetTask : sheetPlayer.tasksDone) {
                databaseSubmissions.putIfAbsent(sheetTask.name, Collections.emptyList());
            }
            List<Pair<String, Integer>> databaseTasks = new ArrayList<>();
            for (Map.Entry<String, List<OldSubmission>> entry : databaseSubmissions.entrySet()) {
                int pointsEarned = 0;
                for (OldSubmission submission : entry.getValue()) {
                    pointsEarned += submission.score;
                }
                databaseTasks.add(new Pair<>(entry.getKey(), pointsEarned));
            }
            for (Pair<String, Integer> databaseTask : databaseTasks) {
                // find the matching sheetTask and compare
                for (TaskSimpleCompleted sheetTask : sheetPlayer.tasksDone) {
                    if (databaseTask.getKey().equals(sheetTask.name)) {
                        // we found the corresponding task
                        if (databaseTask.getValue() == sheetTask.pointsEarned) {
                            // this is a match
                            continue;
                        }
                        logs.add(
                                String.format("Correcting %s's sheet task %s having %d points while database task had %d points",
                                        databasePlayer.name,
                                        databaseTask.getKey(),
                                        sheetTask.pointsEarned,
                                        databaseTask.getValue()
                                )
                        );
                        int sheetTaskIndex = 0;
                        for (TaskSimple sheetTaskHeader : sheetTasks) {
                            if (sheetTaskHeader.name.equals(sheetTask.name)) {
                                Request sheetRequest = SheetsPlayerStats.getRequestUpdateCell(
                                        sheetTaskIndex,
                                        sheetPlayer.getRow(),
                                        databaseTask.getValue());
                                sheetRequests.add(sheetRequest);
                                break;
                            }
                            sheetTaskIndex++;
                        }
                        break;
                    }
                }
                break;
            }
            // update the soul juice of the player
            if (sheetPlayer.getSoulJuice() != databasePlayer.getSoulJuice()) {
                Request sheetRequest = SheetsPlayerStats.getRequestUpdateCell(
                        -3,
                        sheetPlayer.getRow(),
                        databasePlayer.getSoulJuice());
                sheetRequests.add(sheetRequest);
                logs.add(String.format("Correcting %s's sheet soulJuice having %d points while database task had %d points",
                        sheetPlayer.getName(), sheetPlayer.getSoulJuice(), databasePlayer.getSoulJuice()));
            }

        }
        if (!sheetRequests.isEmpty()) {
            ExcursionMain.service.spreadsheets().batchUpdate(SheetsConstants.SPREADSHEET_ID, new BatchUpdateSpreadsheetRequest().setRequests(sheetRequests)).execute();
        }

        return logs;
    }

    private static class ProfileWithSubmission {
        private final List<TaskSimpleCompleted> taskScores;
        private final String guildName;
        private final String guildTag;
        private List<SubmissionData> submissions = new ArrayList<>();
        private final String playerName;
        private final long playerId;

        public ProfileWithSubmission(Profile profile) {
            taskScores = profile.tasksDone;
            this.playerId = profile.getId();
            this.playerName = profile.getName();
            this.guildTag = profile.getGuildTag();
            this.guildName = profile.getGuild();
        }

        public void addSubmission(SubmissionData submission) {
            submissions.add(submission);
        }

        public void sortSubmissions(List<Task> allTasks) {
            final List<SubmissionData> submissionsEnd = new ArrayList<>();
            for (Task task : allTasks) {
                // find the corresponding TaskSimpleCompleted
                TaskSimpleCompleted taskSimpleCompleted = null;
                for (TaskSimpleCompleted completed : taskScores) {
                    if (completed.name.equalsIgnoreCase(task.name)) {
                        taskSimpleCompleted = completed;
                    }
                }
                if (taskSimpleCompleted == null) {
                    // the player hasn't done this task
                    taskSimpleCompleted = new TaskSimpleCompleted(task.points, task.name, task.category, 0);
                }

                // find the corresponding SubmissionData
                List<SubmissionData> submissionsWithName = new ArrayList<>(submissions);
                submissionsWithName.removeIf(submissionData -> !submissionData.getTaskName().equalsIgnoreCase(task.name));

                int scoreEarnedThroughDms = submissionsWithName.size() * task.points;
                int scoreEarnedThroughSheet = taskSimpleCompleted.pointsEarned;
                if (scoreEarnedThroughDms == scoreEarnedThroughSheet) {
                    // if there is no error, cool
                    submissionsEnd.addAll(submissionsWithName);
                    continue;
                }
                if ((scoreEarnedThroughDms - scoreEarnedThroughSheet) % task.points == 0
                        || (scoreEarnedThroughSheet - scoreEarnedThroughDms) % task.points == 0) {
                    // if we simply have too many or too few submissions
                    if (scoreEarnedThroughDms < scoreEarnedThroughSheet) {
                        // add more submissions
                        long date = findPopularDate(submissionsWithName);
                        while (scoreEarnedThroughDms != scoreEarnedThroughSheet) {
                            submissionsWithName.add(new SubmissionData(
                                    true,
                                    true,
                                    -1,
                                    date,
                                    null,
                                    Collections.emptyList(),
                                    new TaskSimple(task.points, task.name, task.category),
                                    SubmissionData.TaskSubmissionType.SYNC,
                                    playerName,
                                    playerId,
                                    Collections.singletonList(new Pair<>(playerId, playerName)),
                                    Collections.emptyList(),
                                    ColoredName.getGuestColor()
                            ));
                            scoreEarnedThroughDms += task.points; // this really should break since mod points = 0
                        }
                    } else {
                        // remove more submissions
                        while (scoreEarnedThroughDms != scoreEarnedThroughSheet) {
                            long date = findPopularDate(submissionsWithName);
                            for (SubmissionData submissionData : submissionsWithName) {
                                if (submissionData.getTimeEpoch() == date) {
                                    submissionsWithName.remove(submissionData);
                                    break;
                                }
                            }
                            scoreEarnedThroughDms -= task.points;
                        }
                    }
                } else {
                    submissionsWithName = Collections.emptyList();
                    System.err.printf("%s has %d sheet points and %d dms points for %s of %d ep.\n", playerName, scoreEarnedThroughSheet, scoreEarnedThroughDms, task.name, task.points);
                }
                submissionsEnd.addAll(submissionsWithName);
            }
            this.submissions = submissionsEnd;
        }

        private static long findPopularDate(List<SubmissionData> submissions) {
            if (submissions.isEmpty()) return CalendarMessage.EPOCH_BEFORE_START_OF_SUBMISSION_HISTORY;
            List<Long> dates = submissions.stream().map(SubmissionData::getTimeEpoch).collect(Collectors.toList());
            Map<Long, Integer> dateToCount = new HashMap<>();
            for (Long date : dates) {
                dateToCount.putIfAbsent(date, 0);
                dateToCount.put(date, dateToCount.get(date) + 1);
            }
            long popularDate = 0;
            int popularCount = 0;
            for (Map.Entry<Long, Integer> date : dateToCount.entrySet()) {
                if (date.getValue() > popularCount) {
                    popularCount = date.getValue();
                    popularDate = date.getKey();
                }
            }
            return popularDate;
        }
    }
}
