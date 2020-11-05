package apple.excursion.discord.reactions.messages.benchmark;

import apple.excursion.database.queries.GetCalendarDB;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.DailyTaskWithDate;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarMessage implements ReactableMessage {
    public static final long EPOCH_BEFORE_START_OF_SUBMISSION_HISTORY = 1590969600000L;
    public static final int EPOCH_YEAR = 2020;
    public static final long EPOCH_START_OF_SUBMISSION_HISTORY = 1593561600000L;// 2020 start of Yin 1.0
    public static final long EPOCH_START_OF_EXCURSION = 1546300800000L;// 2019
    private final Message message;
    Calendar calendar = Calendar.getInstance();
    private long lastUpdated = System.currentTimeMillis();
    private final List<Task> tasks = SheetsTasks.getTasks();
    private final int originalMonth = calendar.get(Calendar.MONTH);
    private final int originalYear = calendar.get(Calendar.YEAR);
    private final int originalDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    private final int originalDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    private final int weeksSinceEpoch = calendar.get(Calendar.WEEK_OF_YEAR) + (calendar.get(Calendar.YEAR) - EPOCH_YEAR) * 365;

    public CalendarMessage(MessageChannel channel) {
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private String makeMessage() {
        Calendar now = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);

        List<DailyTaskWithDate> week = GetCalendarDB.getWeek(calendar);
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("Week %d\n", weeksSinceEpoch));
        text.append(getDash());
        int dayOfWeek = originalDayOfWeek - 1;
        if (dayOfWeek == 0) dayOfWeek = 7; // sunday is sunday
        text.append(String.format("| %-20s    %-23s| %-4s|\n",
                String.format("%s %d %s",
                        DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        originalDayOfMonth,
                        Month.of(originalMonth + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH)),
                "", ""
        ));
        int hoursToTomorrow = (int) ((tomorrow.toInstant().getEpochSecond() - now.toInstant().getEpochSecond()) / 60f / 60);
        text.append(String.format("| %-47s| %-4s|\n",
                String.format("%d hour%s to day change", hoursToTomorrow, hoursToTomorrow == 1 ? "" : "s"),
                ""));
        text.append(getDash());
        text.append(getDash());
        text.append(String.format("| %20s  | %-23s| %-4s|\n", "", "Daily Tasks", "EP"));
        text.append(getDash());
        List<DailyTaskWithDate> weekend = new ArrayList<>();
        for (DailyTaskWithDate day : week) {
            if (day == null) {
                if (!weekend.isEmpty()) {
                    addWeekendInfo(text, weekend);
                }
                text.append(String.format("| %-47s| %-4s|\n", "This date is currently not available", ""));
                text.append(getDash());
                continue;
            }
            if (day.isWeekend()) {
                weekend.add(day);
                continue;
            } else if (!weekend.isEmpty()) {
                // add the weekend info
                addWeekendInfo(text, weekend);
            }
            day.tasks.sort(((o1, o2) -> getTaskScore(o2) - getTaskScore(o1)));
            final int size = day.tasks.size();
            for (int i = 0; i < size; i++) {
                String task = day.tasks.get(i);
                if (i == 0) {
                    text.append(String.format("| %-20s  | %-23s| %-4s|\n",
                            String.format("%s %d %s", day.dayOfWeek, day.dayOfMonth, day.month),
                            task,
                            getTaskScore(task)));
                } else {
                    text.append(String.format("| %20s  | %-23s| %-4s|\n", "", task, getTaskScore(task)));
                }
            }
            text.append(getDash());
        }
        // deal with weekend
        addWeekendInfo(text, weekend);
        text.append("\n```");
        return text.toString();
    }

    private static String getDash() {
        return "-".repeat(56) + "\n";
    }

    private int getTaskScore(String taskName) {
        for (Task task : tasks)
            if (task.name.equalsIgnoreCase(taskName)) return task.points;
        return -1;
    }

    private void addWeekendInfo(StringBuilder messageText, List<DailyTaskWithDate> weekend) {
        if (weekend.isEmpty()) return;
        if (weekend.size() == 1) {
            DailyTaskWithDate day = weekend.get(0);
            final int size = day.tasks.size();
            for (int i = 0; i < size; i++) {
                day.tasks.sort(((o1, o2) -> getTaskScore(o2) - getTaskScore(o1)));
                String task = day.tasks.get(i);
                if (i == 0) {
                    messageText.append(String.format("| %-20s  | %-23s| %-4s|\n",
                            String.format("%s %d %s", day.dayOfWeek, day.dayOfMonth, day.month),
                            task,
                            getTaskScore(task)));
                } else {
                    messageText.append(String.format("| %20s  | %-23s| %-4s|\n", "", task, getTaskScore(task)));
                }
            }
            return;
        }
        List<DailyTaskWithDate> firstMonthWeekend = new ArrayList<>();
        List<DailyTaskWithDate> lastMonthWeekend = new ArrayList<>();

        for (Iterator<DailyTaskWithDate> dayIterator = weekend.iterator(); dayIterator.hasNext(); ) {
            DailyTaskWithDate day = dayIterator.next();
            if (firstMonthWeekend.isEmpty())
                firstMonthWeekend.add(day);
            else if (firstMonthWeekend.get(0).month.equals(day.month))
                firstMonthWeekend.add(day);
            else
                lastMonthWeekend.add(day);
            dayIterator.remove();
        }
        List<String> weekendTasks = new ArrayList<>();
        for (DailyTaskWithDate day : firstMonthWeekend) {
            weekendTasks.addAll(day.tasks);
        }
        weekendTasks.sort(((o1, o2) -> getTaskScore(o2) - getTaskScore(o1)));
        for (int i = 0; i < weekendTasks.size(); i++) {
            String task = weekendTasks.get(i);
            if (i < firstMonthWeekend.size()) {
                DailyTaskWithDate day = firstMonthWeekend.get(i);
                messageText.append(String.format("| %-20s  | %-23s| %-4s|\n",
                        String.format("%s %d %s", day.dayOfWeek, day.dayOfMonth, day.month),
                        task,
                        getTaskScore(task)));
            } else {
                messageText.append(String.format("| %20s  | %-23s| %-4s|\n", "", task, getTaskScore(task)));
            }
        }
        messageText.append(getDash());

        if (lastMonthWeekend.isEmpty()) return;
        if (lastMonthWeekend.size() == 1) {
            DailyTaskWithDate day = lastMonthWeekend.get(0);
            day.tasks.sort(((o1, o2) -> getTaskScore(o2) - getTaskScore(o1)));
            final int size = day.tasks.size();
            for (int i = 0; i < size; i++) {
                String task = day.tasks.get(i);
                if (i == 0) {
                    messageText.append(String.format("| %-20s  | %-23s| %-4s|\n",
                            String.format("%s %d %s", day.dayOfWeek, day.dayOfMonth, day.month),
                            task,
                            getTaskScore(task)));
                } else {
                    messageText.append(String.format("| %20s  | %-23s| %-4s|\n", "", task, getTaskScore(task)));
                }
            }
            return;
        }
        weekendTasks = new ArrayList<>();
        for (DailyTaskWithDate day : lastMonthWeekend) {
            weekendTasks.addAll(day.tasks);
        }
        weekendTasks.sort(((o1, o2) -> getTaskScore(o2) - getTaskScore(o1)));
        for (int i = 0; i < weekendTasks.size(); i++) {
            String task = weekendTasks.get(i);
            if (i < lastMonthWeekend.size()) {
                DailyTaskWithDate day = lastMonthWeekend.get(i);
                messageText.append(String.format("| %-20s  | %-23s| %-4s|\n",
                        String.format("%s %d %s", day.dayOfWeek, day.dayOfMonth, day.month),
                        task,
                        getTaskScore(task)));
            } else {
                messageText.append(String.format("| %20s  | %-23s| %-4s|\n", "", task, getTaskScore(task)));
            }
        }
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case LEFT:
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                message.editMessage(makeMessage()).queue();
                lastUpdated = System.currentTimeMillis();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                if (originalMonth > calendar.get(Calendar.MONTH) || originalYear > calendar.get(Calendar.YEAR)) {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1);
                } else {
                    message.editMessage(makeMessage()).queue();
                }
                lastUpdated = System.currentTimeMillis();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                calendar = Calendar.getInstance();
                lastUpdated = System.currentTimeMillis();
                message.editMessage(makeMessage()).queue();
                event.getReaction().removeReaction(user).queue();
                break;
        }
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void dealWithOld() {
        message.clearReactions().queue(success -> {
        }, failure -> {
        }); //ignore if we don't have perms. it's really not a bad thing
    }
}
