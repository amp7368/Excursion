package apple.excursion.discord.data.answers;

import java.util.List;

public class DailyTaskWithDate {
    public final List<String> tasks;
    public final String dayOfWeek;
    public final int dayOfMonth;
    public final String month;
    private final int dayOfWeekInt;

    public DailyTaskWithDate(List<String> tasks, int dayOfWeek, int dayOfMonth, String month) {
        this.tasks = tasks;
        this.dayOfWeekInt = dayOfWeek;
        switch (dayOfWeek) {
            case 1:
                this.dayOfWeek = "Monday";
                break;
            case 2:
                this.dayOfWeek = "Tuesday";
                break;
            case 3:
                this.dayOfWeek = "Wednesday";
                break;
            case 4:
                this.dayOfWeek = "Thursday";
                break;
            case 5:
                this.dayOfWeek = "Friday";
                break;
            case 6:
                this.dayOfWeek = "Saturday";
                break;
            case 7:
                this.dayOfWeek = "Sunday";
                break;
            default:
                this.dayOfWeek = "???";
                break;
        }
        this.dayOfMonth = dayOfMonth;
        this.month = month;
    }
    public boolean isWeekend(){
        return dayOfWeekInt == 5 || dayOfWeekInt == 6 || dayOfWeekInt ==7;
    }
}
