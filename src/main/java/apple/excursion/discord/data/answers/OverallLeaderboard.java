package apple.excursion.discord.data.answers;

import apple.excursion.discord.data.Profile;

import java.util.List;

public class OverallLeaderboard {
    public List<Profile> leaderboard;

    public OverallLeaderboard(List<Profile> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
