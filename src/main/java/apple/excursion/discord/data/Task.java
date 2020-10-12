package apple.excursion.discord.data;

public class Task {
    public int points;
    public String name;
    public TaskCategory category;

    public Task(int points, String name, String category) {
        this.points = points;
        this.name = name;
        this.category = TaskCategory.valueOf(category.toUpperCase());
    }

    private enum TaskCategory {
        MISSIONS,
        EXCURSIONS,
        DARES
    }
}
