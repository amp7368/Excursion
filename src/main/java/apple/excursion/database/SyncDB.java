package apple.excursion.database;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.sheets.profiles.Profile;
import apple.excursion.utils.Pair;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SyncDB {
    public static final String SYNC_TASK_TYPE = "SYNC";
    public static final String SYNC_TASK_NAME = "SYNC_TASK_NAME";

    public static List<String> sync(List<Profile> sheetData, List<PlayerHeader> databasePlayers, List<GuildHeader> databaseGuilds) throws SQLException {
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
                int score;
                int soulJuice;
                String guildName = sheetPlayer.getGuild();
                String guildTag = sheetPlayer.getGuildTag();
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
                int pointsToAddToDatabase = sheetPlayer.getTotalEp() - score;
                int juiceToAddToDatabase = sheetPlayer.getSoulJuice() - soulJuice;
                if (juiceToAddToDatabase > 0) {
                    sql = GetSql.getSqlUpdatePlayerSoulJuice(sheetPlayer.getId(), juiceToAddToDatabase);
                    statement.addBatch(sql);
                    logs.add(String.format("Added %d SoulJuice to <%d,%s>",
                            juiceToAddToDatabase,
                            sheetPlayer.getId(),
                            sheetPlayer.getName()));
                }
                if (pointsToAddToDatabase > 0) {
                    sql = GetSql.getSqlInsertSubmission(sheetPlayer.getId(), pointsToAddToDatabase);
                    statement.addBatch(sql);
                    sql = GetSql.getSqlInsertSubmissionLink(VerifyDB.currentSubmissionId,
                            sheetPlayer.getId(),
                            guildTag.isBlank() ? VerifyDB.DEFAULT_GUILD_TAG : guildTag);
                    statement.addBatch(sql);
                    logs.add(String.format("Added submission %d of %d EP for <%d,%s> in %s [%s]",
                            VerifyDB.currentSubmissionId,
                            pointsToAddToDatabase,
                            sheetPlayer.getId(),
                            sheetPlayer.getName(),
                            guildName,
                            guildTag));
                    VerifyDB.currentSubmissionId++;
                }
            }
            statement.executeBatch();
        }
        return logs;
    }
}
