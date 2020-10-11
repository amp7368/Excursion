package apple.excursion.discord.data.leaderboard;

import apple.excursion.sheets.LeaderBoardSheet;
import apple.excursion.sheets.SheetsPlayerStats;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LeaderBoard {
    public static List<LeaderBoardEntry> leaderBoardEntries;

    public static void update() {
        leaderBoardEntries = new ArrayList<>();
        List<List<Object>> everyone = null;
        try {
            everyone = LeaderBoardSheet.getEveryone();
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
            leaderBoardEntries.add(new LeaderBoardEntry(everyone.get(i), endIndex));
        }
        leaderBoardEntries.sort((o1, o2) -> o2.points - o1.points);
    }
}
