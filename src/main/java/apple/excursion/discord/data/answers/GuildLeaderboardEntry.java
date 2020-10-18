package apple.excursion.discord.data.answers;

import apple.excursion.database.VerifyDB;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public int score;
    public String topPlayer;
    public int topPlayerPoints;
    public int topGuildScore;
    public int rank;

    public GuildLeaderboardEntry(String guildTag, String guildName, int guildScore, String playerName, int playerScore) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.score = guildScore;
        this.topPlayer = playerName;
        this.topPlayerPoints = playerScore;
    }

    public boolean isDefault() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG);
    }

    public double getProgress() {
        return ((double) this.score) / this.topGuildScore;
    }
}
