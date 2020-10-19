package apple.excursion.database;

import apple.excursion.discord.data.answers.DailyTaskWithDate;
import apple.excursion.utils.Pretty;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.YearMonth;
import java.util.*;

public class GetCalendarDB {
    private static final Collection<MonthWithTasks> tasksInMonths = new ArrayList<>();

    public static List<String> getTasksToday(Calendar day) {
        List<DailyTaskWithDate> tasks = getWeek(day);
        int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
        if (DailyTaskWithDate.isWeekend(dayOfWeek - 1)) { // because of sunday
            List<DailyTaskWithDate> weekend = new ArrayList<>();
            if (dayOfWeek == Calendar.SUNDAY) {
                // go backwards from sunday
                weekend.add(tasks.get(6));
                if (tasks.get(5).month.equals(weekend.get(0).month)) {
                    weekend.add(tasks.get(5));
                    if (tasks.get(4).month.equals(weekend.get(0).month))
                        weekend.add(tasks.get(4));
                }
            } else if (dayOfWeek == Calendar.SATURDAY) {
                weekend.add(tasks.get(5));
                if (tasks.get(6).month.equals(weekend.get(0).month))
                    weekend.add(tasks.get(6));
                if (tasks.get(4).month.equals(weekend.get(0).month))
                    weekend.add(tasks.get(4));
            } else if (dayOfWeek == Calendar.FRIDAY) {
                weekend.add(tasks.get(4));
                if (tasks.get(5).month.equals(weekend.get(0).month)) {
                    weekend.add(tasks.get(5));
                    if (tasks.get(6).month.equals(weekend.get(0).month))
                        weekend.add(tasks.get(6));
                }
            }
            List<String> taskNames = new ArrayList<>();
            for (DailyTaskWithDate task : weekend) {
                taskNames.addAll(task.tasks);
            }
            return taskNames;
        } else {
            int index = (dayOfWeek - 2); // because of sunday
            if (index == -1) //sunday
                index += 7;
            return tasks.get(index).tasks;
        }
    }

    public synchronized static List<DailyTaskWithDate> getWeek(Calendar calendar) {
        try {
            VerifyDB.verifyCalendar();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        final YearMonth yearMonth = YearMonth.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
        List<DailyTaskWithDate> tasks = new ArrayList<>();
        int lengthOfMonth = yearMonth.lengthOfMonth();
        int lengthOfLastMonth = 0;
        int dayOfWeekInCalendar = calendar.get(Calendar.DAY_OF_WEEK) - 2; // what day of the week it is (-1 for first day of week being monday. -1 to make it 0 index)
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_MONTH) - dayOfWeekInCalendar - 1; // the first day of the week in this month (-1 to make it 0 index)
        int lastDayOfWeek = firstDayOfWeek + 7; // the last day of the week for this week (+7 for days in a week. +1 for exclusiveness -1 for 0 index)
        int dayOfWeek, dayOfMonth, lower, upper;
        dayOfWeek = 0;
        lower = firstDayOfWeek;
        dayOfMonth = firstDayOfWeek;
        upper = lastDayOfWeek;

        // unless the month does not have any tasks,
        // lower month is guaranteed, but upper month might be null because there may only be one month in a week
        @Nullable MonthWithTasks lowerMonth, upperMonth;
        if (lower < 0) {
            // get this month (upper month)
            upperMonth = getMonthWithTask(calendar.toInstant().toEpochMilli(), yearMonth);

            // get last month
            Calendar lastMonthCalendar = (Calendar) calendar.clone();
            lastMonthCalendar.add(Calendar.MONTH, -1);
            YearMonth tempYearMonth = yearMonth.minusMonths(1);
            lowerMonth = getMonthWithTask(lastMonthCalendar.toInstant().toEpochMilli(), tempYearMonth);
            lengthOfLastMonth = tempYearMonth.lengthOfMonth();
        } else {
            // get this month
            lowerMonth = getMonthWithTask(calendar.toInstant().toEpochMilli(), yearMonth);

            // get next month if we need it
            if (upper > lengthOfMonth) {
                Calendar nextMonthCalendar = (Calendar) calendar.clone();
                nextMonthCalendar.add(Calendar.MONTH, 1);

                YearMonth tempYearMonth = yearMonth.plusMonths(1);
                upperMonth = getMonthWithTask(nextMonthCalendar.toInstant().toEpochMilli(), tempYearMonth);
            } else {
                upperMonth = null;
            }
        }
        if (lower < 0) {
            int lastMonthLower = lower + lengthOfLastMonth;
            dayOfMonth = dayOfMonth + lengthOfLastMonth;
            if (lowerMonth == null) {
                for (; lower < 0; lower++, lastMonthLower++, dayOfWeek++, dayOfMonth++)
                    tasks.add(null);
            } else {
                for (; lower < 0; lower++, lastMonthLower++, dayOfWeek++, dayOfMonth++) {
                    tasks.add(new DailyTaskWithDate(
                            lowerMonth.tasks[lastMonthLower],
                            dayOfWeek + 1,
                            dayOfMonth + 1,
                            lowerMonth.prettyFullMonthName,
                            lowerMonth.monthInt,
                            lowerMonth.yearInt
                    ));
                }
            }
            dayOfMonth = 0;
            if (upperMonth == null) {
                for (; lower < upper; lower++, dayOfWeek++, dayOfMonth++)
                    tasks.add(null);
            } else {
                for (; lower < upper; lower++, dayOfWeek++, dayOfMonth++) {
                    tasks.add(new DailyTaskWithDate(
                            upperMonth.tasks[lower],
                            dayOfWeek + 1,
                            dayOfMonth + 1,
                            upperMonth.prettyFullMonthName,
                            upperMonth.monthInt,
                            upperMonth.yearInt
                    ));
                }
            }
        } else {
            int lowerMonthUpper = Math.min(upper, lengthOfMonth);
            if (lowerMonth == null) {
                for (; lower < lowerMonthUpper; lower++, dayOfWeek++, dayOfMonth++) {
                    tasks.add(null);
                }
            } else {
                for (; lower < lowerMonthUpper; lower++, dayOfWeek++, dayOfMonth++) {
                    tasks.add(new DailyTaskWithDate(
                            lowerMonth.tasks[lower],
                            dayOfWeek + 1,
                            dayOfMonth + 1,
                            lowerMonth.prettyFullMonthName,
                            lowerMonth.monthInt,
                            lowerMonth.yearInt
                    ));
                }
            }
            dayOfMonth = 0;
            if (lower != upper) {
                if (upperMonth == null) {
                    for (; lower < upper; lower++, dayOfWeek++, dayOfMonth++)
                        tasks.add(null);
                } else {
                    for (int upperMonthLower = 0; lower < upper; lower++, upperMonthLower++, dayOfWeek++, dayOfMonth++) {
                        tasks.add(new DailyTaskWithDate(
                                upperMonth.tasks[upperMonthLower],
                                dayOfWeek + 1,
                                dayOfMonth + 1,
                                upperMonth.prettyFullMonthName,
                                upperMonth.monthInt,
                                upperMonth.yearInt
                        ));
                    }
                }
            }
        }
        return tasks;
    }


    @Nullable
    private static MonthWithTasks getMonthWithTask(long epochMillis, YearMonth yearMonth) {
        String monthName = VerifyDB.getMonthFromDate(epochMillis);
        for (MonthWithTasks monthWithTasks : tasksInMonths) {
            if (monthWithTasks.monthName.equals(monthName)) {
                // we already have this month
                monthWithTasks.lastUpdated = System.currentTimeMillis();
                tasksInMonths.add(monthWithTasks);
                return monthWithTasks;
            }
        }
        tasksInMonths.removeIf(MonthWithTasks::isOld);
        try {
            return new MonthWithTasks(monthName, yearMonth.lengthOfMonth(), yearMonth.getMonth().name(), yearMonth.getMonthValue(), yearMonth.getYear());
        } catch (SQLException e) {
            // this is fine. the month was just not filled in
            try {
                return new MonthWithTasks(monthName, yearMonth.lengthOfMonth(), yearMonth.getMonth().name(), yearMonth.getMonthValue(), yearMonth.getYear());
            } catch (SQLException e1) {
                return null;
            }
        }
    }

    private static class MonthWithTasks {
        private static final long OLD_CACHE = 1000 * 60 * 60 * 2; //2 hrs
        private final int monthInt;
        private final int yearInt;
        private long lastUpdated = System.currentTimeMillis();
        private final String monthName;
        private final List<String>[] tasks;
        private final String prettyFullMonthName;

        private MonthWithTasks(String monthName, int daysThisMonth, String prettyFullMonthName, int monthInt, int yearInt) throws SQLException {
            this.monthName = monthName;
            this.monthInt = monthInt;
            this.yearInt = yearInt;
            this.prettyFullMonthName = Pretty.upperCaseFirst(prettyFullMonthName);
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
