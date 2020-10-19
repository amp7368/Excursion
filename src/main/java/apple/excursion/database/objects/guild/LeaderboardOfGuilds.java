package apple.excursion.database.objects.guild;

import java.util.*;

public class LeaderboardOfGuilds {
    private List<GuildLeaderboardEntry> leaderboard;
    private GuildLeaderboardEntry noGuildsEntry;
    private long totalEp;

    public LeaderboardOfGuilds(List<GuildLeaderboardEntry> guilds) {
        this.leaderboard = guilds;
        Iterator<GuildLeaderboardEntry> iterator = leaderboard.iterator();
        while (iterator.hasNext()) {
            GuildLeaderboardEntry guild = iterator.next();
            if (guild.isDefault()) {
                noGuildsEntry = guild;
                iterator.remove();
                break; // only 1 default guild entry
            }
        }
        int topScore;
        if (leaderboard.isEmpty()) {
            topScore = Integer.MAX_VALUE;
        } else {
            leaderboard.sort((o1, o2) -> o2.score - o1.score);
            topScore = guilds.get(0).score;
        }
        int totalEp = 0;
        final int size = leaderboard.size();
        for (int i = 0; i < size; i++) {
            GuildLeaderboardEntry guild = leaderboard.get(i);
            guild.rank = i + 1;
            guild.topGuildScore = topScore;
            totalEp += guild.score;
        }
        this.totalEp = totalEp;
    }

    public long getTotalEp() {
        return totalEp;
    }

    public long getNoGuildsEp() {
        return noGuildsEntry.score;
    }

    public int size() {
        return leaderboard.size();
    }

    public GuildLeaderboardEntry get(int i) {
        return leaderboard.get(i);
    }

    public GuildLeaderboardEntry get(String tag, String name) {
        for (GuildLeaderboardEntry guildLeaderboardEntry : leaderboard) {
            if (guildLeaderboardEntry.guildTag.equals(tag)) {
                return guildLeaderboardEntry;
            }
        }
        for (GuildLeaderboardEntry guild : leaderboard) {
            if (guild.guildName.equals(name)) {
                return guild;
            }
        }
        return null;
    }
}
