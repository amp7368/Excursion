package apple.excursion.database;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.discord.data.TaskSimpleCompleted;
import apple.excursion.sheets.profiles.Profile;
import apple.excursion.utils.Pair;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
                    System.out.println(sql);
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
                if(playerData == null)
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
                        logs.add(String.format("Added submission %s~~%d of %d EP for <%d,%s> in %s [%s]",
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
            statement.executeBatch();
            statement.close();
        }
        return logs;
    }
}
