package apple.excursion.database.objects.player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerLeaderboard {
    private final List<PlayerLeaderboardEntry> leaderboard;

    public PlayerLeaderboard(List<PlayerLeaderboardEntry> entries) {
        leaderboard = entries;
        if (leaderboard.isEmpty()) {
            return;
        }
        leaderboard.sort((o1, o2) -> o2.score - o1.score);
        final int size = leaderboard.size();
        long topPlayerScore = 0;
        for (PlayerLeaderboardEntry entry : leaderboard)
            topPlayerScore = Math.max(entry.score, topPlayerScore);
        for (int i = 0; i < size; i++) {
            PlayerLeaderboardEntry entry = leaderboard.get(i);
            entry.rank = i;
            entry.topPlayerScore = topPlayerScore;
        }
    }

    public PlayerLeaderboardEntry get(int i) {
        return leaderboard.get(i);
    }

    @Nullable
    public PlayerLeaderboardEntry get(long id) {
        for (PlayerLeaderboardEntry entry : leaderboard) {
            if (entry.hasId(id)) return entry;
        }
        return null;
    }

    public int size() {
        return leaderboard.size();
    }

    public List<PlayerLeaderboardEntry> getPlayersWithName(String name) {
        List<PlayerLeaderboardEntry> players = new ArrayList<>();
        for (PlayerLeaderboardEntry entry : leaderboard) {
            if (entry.nameIsSimilar(name)) {
                players.add(entry);
            }
        }
        return players;
    }

    public List<PlayerLeaderboardEntry> getPlayersById(List<Long> ids) {
        List<PlayerLeaderboardEntry> players = new ArrayList<>();
        for (Long id : ids) {
            for (PlayerLeaderboardEntry entry : leaderboard) {
                if (entry.hasId(id)) {
                    players.add(entry);
                    break;
                }
            }
        }
        return players;
    }
}
