package apple.excursion.discord.data;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Collection;

public class Task {
    public int points;
    public String name;
    public String category;

    public Task(int points, String name, String category) {
        this.points = points;
        this.name = name;
        this.category = category.toLowerCase();
        TaskCategory.taskTypes.add(this.category);
    }

    public static class TaskCategory {
        private static final ConcurrentHashSet<String> taskTypes = new ConcurrentHashSet<>();

        public static Collection<String> values() {
            return new ArrayList<>(taskTypes);
        }
    }
}
