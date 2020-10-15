package apple.excursion.discord.data.answers;

import java.util.Calendar;
import java.util.List;

public class DailyTaskWithDate {
    public final List<String> tasks;
    public final String dayOfWeek;
    public final int dayOfMonth;
    public final String month;
    public final int monthInt;
    public final int yearInt;
    private final int dayOfWeekInt;

    public DailyTaskWithDate(List<String> tasks, int dayOfWeek, int dayOfMonth, String month, int monthInt, int yearInt) {
        this.tasks = tasks;
        this.dayOfWeekInt = dayOfWeek;
        this.monthInt = monthInt - 1; // make it 0 index
        this.yearInt = yearInt;
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

    public boolean isWeekend() {
        return dayOfWeekInt == 5 || dayOfWeekInt == 6 || dayOfWeekInt == 7;
    }

    public static boolean isWeekend(int dayOfWeekInt) {
        return dayOfWeekInt == 5 || dayOfWeekInt == 6 || dayOfWeekInt == 7;
    }

    public boolean isToday(Calendar now) {
        return now.get(Calendar.DAY_OF_MONTH) == this.dayOfMonth &&
                now.get(Calendar.MONTH) == this.monthInt &&
                now.get(Calendar.YEAR) == this.yearInt;
    }
}
