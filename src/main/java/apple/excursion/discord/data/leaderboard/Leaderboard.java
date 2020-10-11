package apple.excursion.discord.data.leaderboard;

import apple.excursion.sheets.LeaderboardSheet;
import apple.excursion.sheets.SheetsPlayerStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Leaderboard {
    public static List<LeaderboardEntry> leaderBoardEntries;
    public static List<GuildLeaderboardEntry> guildLeaderboardEntries;
    private static long totalEpOfEveryone = 0;
    private static GuildLeaderboardEntry noGuildsEntry = null;

    public static void update() {
        leaderBoardEntries = new ArrayList<>();
        guildLeaderboardEntries = new ArrayList<>();
        List<List<Object>> everyone = null;
        try {
            everyone = LeaderboardSheet.getEveryone();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (everyone == null) {
            System.err.println("There is nobody in the spreadsheet or something else went wrong getting the leaderboard");
            System.exit(1);
        }
        int endIndex = 0;
        for (Object header : everyone.get(1)) {
            if (header.equals(SheetsPlayerStats.TASKS_DONE_HEADER))
                break;
            endIndex++;
        }
        for (int i = 4; i < everyone.size(); i++) {
            leaderBoardEntries.add(new LeaderboardEntry(everyone.get(i), endIndex));
        }
        leaderBoardEntries.sort((o1, o2) -> o2.points - o1.points);
        HashMap<String, GuildLeaderboardEntry> guildLeaderboardEntriesMap = new HashMap<>();
        for (LeaderboardEntry entry : leaderBoardEntries) {
            guildLeaderboardEntriesMap.putIfAbsent(entry.guildName,
                    new GuildLeaderboardEntry(entry.guildName, entry.guildTag, entry.name, entry.points));

            final GuildLeaderboardEntry guildLeaderboardEntry = guildLeaderboardEntriesMap.get(entry.guildName);
            guildLeaderboardEntry.updateTop(entry);
            totalEpOfEveryone += entry.points;
            guildLeaderboardEntry.points += entry.points;
        }
        noGuildsEntry = guildLeaderboardEntriesMap.remove("");
        if (noGuildsEntry != null) {
            noGuildsEntry.guildName = "No Guilds";
        }
        guildLeaderboardEntries.addAll(guildLeaderboardEntriesMap.values());
        guildLeaderboardEntries.sort((o1, o2) -> o2.points - o1.points);
    }

    public static long getTotalEp() {
        return totalEpOfEveryone;
    }

    public static long getNoGuildsEp() {
        return noGuildsEntry.points;
    }
}
