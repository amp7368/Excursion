package apple.excursion.discord.data.leaderboard;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public String topPlayer;
    public int topPlayerPoints;
    public int points = 0;

    public GuildLeaderboardEntry(String guildName, String guildTag, String topPlayer, int topPlayerPoints) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.topPlayer = topPlayer;
        this.topPlayerPoints = topPlayerPoints;
    }

    public void updateTop(LeaderboardEntry entry) {
        if (entry.points > topPlayerPoints) {
            topPlayerPoints = entry.points;
            topPlayer = entry.name;
        }
    }
}
