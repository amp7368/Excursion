package apple.excursion.discord.data;

public class TaskSimple {
    public final int points;
    public final String name;
    public final String category;

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
