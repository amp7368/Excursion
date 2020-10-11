package apple.excursion.discord.data.leaderboard;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public int points = 0;

    public GuildLeaderboardEntry(String guildName, String guildTag) {
        this.guildName = guildName;
        this.guildTag = guildTag;
    }
}
