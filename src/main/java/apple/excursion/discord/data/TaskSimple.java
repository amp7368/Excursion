package apple.excursion.discord.data;

import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.ArrayList;
import java.util.Collection;

public class TaskSimple {
    public int points;
    public String name;
    public String category;

    public TaskSimple(int points, String name, String category) {
        this.points = points;
        this.name = name;
        char[] categoryChars = category.toLowerCase().toCharArray();
        if (categoryChars.length != 0 && categoryChars[categoryChars.length - 1] == 's') {
            this.category = category.toLowerCase().substring(0, categoryChars.length - 1);
        } else
            this.category = category.toLowerCase();
    }
}
