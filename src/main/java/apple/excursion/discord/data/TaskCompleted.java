package apple.excursion.discord.data;

public class TaskCompleted extends Task {
    public int pointsEarned;

    public TaskCompleted(int points, String name, String category, int pointsEarned) {
        super(points, name, category);
        this.pointsEarned = pointsEarned;
    }
}
