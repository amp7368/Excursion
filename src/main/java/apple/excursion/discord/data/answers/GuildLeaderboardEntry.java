package apple.excursion.discord.data.answers;

import apple.excursion.database.VerifyDB;
import apple.excursion.discord.data.Profile;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public int score;
    public String topPlayer;
    public int topPlayerPoints;
    public int topGuildScore;
    public int rank;

    public GuildLeaderboardEntry(String guildName, String guildTag, String topPlayer, int topPlayerPoints) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.topPlayer = topPlayer;
        this.topPlayerPoints = topPlayerPoints;
        this.score = topPlayerPoints;
    }

    public GuildLeaderboardEntry(String guildTag, String guildName, int guildScore, String playerName, int playerScore) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.score = guildScore;
        this.topPlayer = playerName;
        this.topPlayerPoints = playerScore;
    }

    public void updateTop(Profile entry) {
        this.score += entry.getTotalEp();
        if (entry.getTotalEp() > topPlayerPoints) {
            topPlayerPoints = entry.getTotalEp();
            topPlayer = entry.getName();
        }
    }

    public boolean isDefault() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG);
    }
}
