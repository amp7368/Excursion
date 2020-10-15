package apple.excursion.discord.data.answers;

import apple.excursion.discord.data.Profile;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public String topPlayer;
    public int topPlayerPoints;
    public long points;

    public GuildLeaderboardEntry(String guildName, String guildTag, String topPlayer, int topPlayerPoints) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.topPlayer = topPlayer;
        this.topPlayerPoints = topPlayerPoints;
        this.points = topPlayerPoints;
    }

    public void updateTop(Profile entry) {
        this.points += entry.getTotalEp();
        if (entry.getTotalEp() > topPlayerPoints) {
            topPlayerPoints = entry.getTotalEp();
            topPlayer = entry.getName();
        }
    }
}
