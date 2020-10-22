package apple.excursion.discord.data.answers;

import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.player.PlayerData;

import java.util.List;

public class HistoryGuildLeaderboard {

    public final GuildLeaderboardEntry matchedGuild;
    public final long startTime;
    public final long endTime;
    public final List<PlayerData> players;
    public final List<OldSubmission> submissions;

    public HistoryGuildLeaderboard(GuildLeaderboardEntry matchedGuild, List<PlayerData> players, List<OldSubmission> submissions, long startTime, long endTime) {
        this.matchedGuild = matchedGuild;
        this.players = players;
        this.submissions = submissions;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
