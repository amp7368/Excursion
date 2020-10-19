package apple.excursion.database.objects.guild;

import apple.excursion.database.VerifyDB;
import apple.excursion.utils.GetColoredName;

public class GuildLeaderboardEntry {
    public String guildName;
    public String guildTag;
    public int score;
    public String topPlayer;
    public int topPlayerPoints;
    public int topGuildScore;
    public int rank;

    public GuildLeaderboardEntry(String guildTag, String guildName, int guildScore, String playerName, long playerId, int playerScore) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.score = guildScore;
        String coloredName = GetColoredName.get(playerId).getName();
        this.topPlayer = coloredName == null ? playerName : coloredName;
        this.topPlayerPoints = playerScore;
    }

    public boolean isDefault() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG);
    }

    public double getProgress() {
        return ((double) this.score) / this.topGuildScore;
    }

    public String getGuildName() {
        return guildName.equals(VerifyDB.DEFAULT_GUILD_NAME) ? "" : guildName;
    }

    public String getGuildTag() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG) ? "" : guildTag;
    }
}
