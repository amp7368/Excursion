package apple.excursion.discord.data.answers;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GuildLeaderboardProfile {
    private GuildLeaderboardEntry guild;
    private long totalEpOfEveryone;
    private int rank;

    public GuildLeaderboardProfile(GuildLeaderboardEntry guild, long totalEpOfEveryone, int rank) {
        this.guild = guild;
        this.totalEpOfEveryone = totalEpOfEveryone;
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public double getProgress() {
        return BigDecimal.valueOf(guild.points).divide(BigDecimal.valueOf(totalEpOfEveryone), 3, RoundingMode.DOWN).doubleValue();
    }

    public long getTotalEp() {
        return guild.points;
    }
}
