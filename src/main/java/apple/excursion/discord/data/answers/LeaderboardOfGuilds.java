package apple.excursion.discord.data.answers;

import apple.excursion.discord.data.Profile;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardOfGuilds {
    public List<GuildLeaderboardEntry> leaderboard;
    private GuildLeaderboardEntry noGuildsEntry;
    private long totalEp;
    private long topEp = -1;

    public LeaderboardOfGuilds(List<Profile> profiles) {
        Map<String, GuildLeaderboardEntry> leaderboard = new HashMap<>();
        for (Profile profile : profiles) {
            if (leaderboard.containsKey(profile.getGuild())) {
                leaderboard.get(profile.getGuild()).updateTop(profile);
            } else {
                leaderboard.put(profile.getGuild(), new GuildLeaderboardEntry(profile.getGuild(), profile.getGuildTag(), profile.getName(), profile.getTotalEp()));
            }
        }
        noGuildsEntry = leaderboard.remove("");
        this.leaderboard = new ArrayList<>(leaderboard.values());
        for (GuildLeaderboardEntry entry : this.leaderboard) {
            totalEp += entry.points;
            if (entry.points > topEp) topEp = entry.points;
        }
        totalEp += noGuildsEntry.points;
        this.leaderboard.sort((o1, o2) -> (int) (o2.points - o1.points));
    }

    public long getTotalEp() {
        return totalEp;
    }

    public long getNoGuildsEp() {
        return noGuildsEntry.points;
    }

    @Nullable
    public GuildLeaderboardProfile getGuildProfile(String guildTag) {
        int rank = 1;
        for (GuildLeaderboardEntry entry : leaderboard) {
            if (entry.guildTag.equals(guildTag))
                return new GuildLeaderboardProfile(entry, totalEp, rank);
            rank++;
        }
        return null;
    }
}
