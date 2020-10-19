package apple.excursion.discord.data.answers;

import apple.excursion.database.objects.guild.LeaderboardOfGuilds;

public class HistoryLeaderboardOfGuilds {

    public final LeaderboardOfGuilds leaderboard;
    public final long startTime;
    public final long endTime;

    public HistoryLeaderboardOfGuilds(LeaderboardOfGuilds leaderboard, long startTime, long endTime) {
        this.leaderboard = leaderboard;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
