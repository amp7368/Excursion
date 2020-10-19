package apple.excursion.discord.data;

public class TaskSimpleCompleted extends TaskSimple {
    public int pointsEarned;

    public TaskSimpleCompleted(int points, String name, String category, int pointsEarned) {
        super(points, name, category);
        this.pointsEarned = pointsEarned;
    }
}