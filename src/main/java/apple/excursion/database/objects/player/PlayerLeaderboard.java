package apple.excursion.database.objects.player;

import java.util.List;

public class PlayerLeaderboard {
    private List<PlayerLeaderboardEntry> leaderboard;
    private long totalEp = 0;

    public PlayerLeaderboard(List<PlayerLeaderboardEntry> entries) {
        leaderboard = entries;
        if (leaderboard.isEmpty()) {
            return;
        }
        leaderboard.sort((o1, o2) -> o2.score - o1.score);
        final int size = leaderboard.size();
        for (int i = 0; i < size; i++) {
            PlayerLeaderboardEntry entry = leaderboard.get(i);
            entry.rank = i;
            totalEp += entry.score;
        }
    }

    public PlayerLeaderboardEntry get(int i) {
        return leaderboard.get(i);
    }

    public PlayerLeaderboardEntry get(long id) {
        for (PlayerLeaderboardEntry entry : leaderboard) {
            if (entry.hasId(id)) return entry;
        }
        return null;
    }

    public int size() {
        return leaderboard.size();
    }
}
