package apple.excursion.discord.data.answers;

import apple.excursion.discord.data.Profile;

import javax.annotation.Nullable;
import java.util.List;

public class OverallLeaderboard {
    public List<Profile> leaderboard;

    public OverallLeaderboard(List<Profile> leaderboard) {
        this.leaderboard = leaderboard;
    }

    @Nullable
    public PlayerLeaderboardProfile getPlayerProfile(long id) {
        int rank = 1;
        for (Profile profile : leaderboard) {
            if (profile.hasId(id))
                return new PlayerLeaderboardProfile(profile, rank);
            rank++;
        }
        return null;
    }
}
