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
                    new GuildLeaderboardEntry(entry.guildName, entry.guildTag));
            guildLeaderboardEntriesMap.get(entry.guildName).points += entry.points;
        }
        GuildLeaderboardEntry nobodies = guildLeaderboardEntriesMap.get("");
        if (nobodies != null) {
            nobodies.guildName = "No Guilds";
            guildLeaderboardEntriesMap.put("No Guilds", guildLeaderboardEntriesMap.remove(""));
        }
        guildLeaderboardEntries.addAll(guildLeaderboardEntriesMap.values());
        guildLeaderboardEntries.sort((o1, o2) -> o2.points - o1.points);
    }
}
