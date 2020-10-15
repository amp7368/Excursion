package apple.excursion.database;

import apple.excursion.discord.data.answers.DailyTaskWithDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.*;

public class GetCalendarDB {
    private static final Object sync = new Object();
    private static final Collection<MonthWithTasks> calendar = new ArrayList<>();

    public static List<DailyTaskWithDate> getWeek(Calendar weekDesired) throws SQLException {
        final YearMonth yearMonth = YearMonth.of(weekDesired.get(Calendar.YEAR), weekDesired.get(Calendar.MONTH) + 1);
        int daysThisMonth = yearMonth.lengthOfMonth();
        String monthName = VerifyDB.getMonthFromDate(weekDesired.getTimeInMillis());
        for (MonthWithTasks monthWithTasks : calendar) {
            if (monthWithTasks.monthName.equals(monthName)) {
                // we already have this month
                monthWithTasks.lastUpdated = System.currentTimeMillis();
                return getWeekFromMonthWithTasks(weekDesired, monthWithTasks, yearMonth);
            }
        }
        final MonthWithTasks monthWithTasks = new MonthWithTasks(monthName, daysThisMonth);
        calendar.add(monthWithTasks);
        calendar.removeIf(MonthWithTasks::isOld);
        return getWeekFromMonthWithTasks(weekDesired, monthWithTasks, yearMonth);
    }

    private static List<DailyTaskWithDate> getWeekFromMonthWithTasks(Calendar calendar, MonthWithTasks monthWithTasks, YearMonth yearMonth) {
        List<DailyTaskWithDate> tasks = new ArrayList<>();
        int dayOfWeekInCalendar = calendar.get(Calendar.DAY_OF_WEEK) - 2; // what day of the week it is (-1 for first day of week being monday. -1 to make it 0 index)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_MONTH) - dayOfWeekInCalendar - 1; // the first day of the week in this month (-1 to make it 0 index)
        int lastDayOfWeek = firstDayOfWeek + 7; // the last day of the week for this week (+7 for days in a week. +1 for exclusiveness -1 for 0 index)
        int dayOfWeek, dayOfMonth, lower, upper;
        if (firstDayOfWeek < 0) {
            dayOfWeek = -firstDayOfWeek;
            lower = 0;
            dayOfMonth = 0;
        } else {
            dayOfWeek = 0;
            lower = firstDayOfWeek;
            dayOfMonth = firstDayOfWeek;
        }
        upper = Math.min(lastDayOfWeek, yearMonth.lengthOfMonth());
        for (; lower < upper; lower++, dayOfWeek++, dayOfMonth++) {
            tasks.add(new DailyTaskWithDate(
                    monthWithTasks.tasks[lower],
                    dayOfWeek + 1,
                    dayOfMonth + 1,
                    yearMonth.getMonth().name()
            ));
        }
        return tasks;
    }

    private static class MonthWithTasks {
        private static final long OLD_CACHE = 1000 * 60 * 60 * 2; //2 hrs
        private long lastUpdated = System.currentTimeMillis();
        private final String monthName;
        private final List<String>[] tasks;

        private MonthWithTasks(String monthName, int daysThisMonth) throws SQLException {
            this.monthName = monthName;
            this.tasks = new List[daysThisMonth];
            String sql = GetSql.getSqlGetCalendar(monthName);
            Statement statement = VerifyDB.calendarDbConnection.createStatement();
            ResultSet response = statement.executeQuery(sql);
            int i = 0;
            // the first is always repeated twice for no reason
            if (!response.isClosed()) response.next();
            while (!response.isClosed()) {
                tasks[i++] = Arrays.asList(GetSql.convertTaskNameFromSql(response.getString(2)).split(","));
                response.next();
            }
            response.close();
            statement.close();

        }

        public boolean isOld() {
            return System.currentTimeMillis() - lastUpdated > OLD_CACHE;
        }
    }
}
