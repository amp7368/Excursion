package apple.excursion.discord.data;

import apple.excursion.utils.GetFromObject;
import org.eclipse.jetty.util.ConcurrentHashSet;

import javax.annotation.Nullable;
import java.util.List;

public class Task {
    public String category; // is lowercase
    public String taskName;
    public String description;
    @Nullable
    public String coordinates;
    public int ep;
    public String createdBy;
    public String repeatable;
    @Nullable
    public String images;
    public boolean isFail;

    public Task(List<Object> row) {
        String category = row.get(0).toString().toLowerCase();
        char[] categoryChars = category.toLowerCase().toCharArray();
        if (categoryChars.length != 0 && categoryChars[categoryChars.length - 1] == 's') {
            this.category = category.toLowerCase().substring(0, categoryChars.length - 1);
        } else
            this.category = category.toLowerCase();
        TaskCategory.taskTypes.add(this.category);
        taskName = row.get(1).toString();
        description = row.get(2).toString();
        coordinates = row.get(3) == null ? null : row.get(0).toString();
        final Object epObject = row.get(4);
        if (epObject != null) {
            final String epString = epObject.toString();
            if (epString.endsWith(" EP"))
                ep = GetFromObject.getInt(epString.substring(0, epString.length() - 3));
            else
                ep = GetFromObject.getInt(epObject);
        }
        createdBy = row.get(5).toString();
        repeatable = row.get(6).toString();
        images = row.size() < 8 ? null : row.get(7).toString();
        isFail = GetFromObject.intFail(ep);
    }

    @Override
    public int hashCode() {
        return taskName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Task && ((Task) other).taskName.equalsIgnoreCase(taskName);
    }

    public static class TaskCategory {
        private static final ConcurrentHashSet<String> taskTypes = new ConcurrentHashSet<>();

    }
}
