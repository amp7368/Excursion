package apple.excursion.discord.data.leaderboard;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class LeaderBoard {
    public static HashMap<Integer, LeaderBoardEntry> leaderBoardEntries = new HashMap<>();

    public static void update(@Nullable List<List<Object>> entries) {
        if (entries == null) return;

        HashMap<Integer, LeaderBoardEntry> entriesTemp = new HashMap<>();
        int i = 1;
        for (List<Object> entry : entries) {
            entriesTemp.put(i, new LeaderBoardEntry(entry));
            i++;
        }
        leaderBoardEntries = entriesTemp;

    }
}
