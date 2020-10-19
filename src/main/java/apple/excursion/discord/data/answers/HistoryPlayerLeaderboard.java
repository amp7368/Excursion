package apple.excursion.discord.data.answers;

import apple.excursion.database.objects.player.PlayerLeaderboard;

public class HistoryPlayerLeaderboard {
    public final PlayerLeaderboard leaderboard;
    public final long startTime;
    public final long endTime;

    public HistoryPlayerLeaderboard(PlayerLeaderboard leaderboard, long startTime, long endTime) {
        this.leaderboard = leaderboard;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
