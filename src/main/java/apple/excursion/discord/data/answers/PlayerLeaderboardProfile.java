package apple.excursion.discord.data.answers;

import apple.excursion.discord.data.Profile;
import apple.excursion.discord.data.TaskSimple;

import java.util.List;


public class PlayerLeaderboardProfile {
    private Profile profile;
    private int rank;

    public PlayerLeaderboardProfile(Profile profile, int rank) {
        this.profile = profile;
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public double getProgress() {
        return profile.getProgress();
    }

    public int getCountTasksDone() {
        return profile.getCountTasksDone();
    }

    public int getCountTasksTotal() {
        return profile.getCountTasksTotal();
    }

    public int getTotalEp() {
        return profile.getTotalEp();
    }

    public List<TaskSimple> getTopTasks(String taskType) {
        return profile.getTopTasks(taskType);
    }
}
