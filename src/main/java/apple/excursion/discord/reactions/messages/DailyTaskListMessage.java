package apple.excursion.discord.reactions.messages;

import apple.excursion.database.GetCalendarDB;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.DailyTaskWithDate;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class DailyTaskListMessage implements ReactableMessage {
    private final Message message;
    Calendar calendar = Calendar.getInstance();
    private long lastUpdated = System.currentTimeMillis();
    private final List<Task> tasks = SheetsTasks.getTasks();
    private final int originalMonth = calendar.get(Calendar.MONTH);
    private final int originalYear = calendar.get(Calendar.MONTH);

    public DailyTaskListMessage(MessageChannel channel) {
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private String makeMessage() {
        Calendar now = Calendar.getInstance();
        List<DailyTaskWithDate> week = GetCalendarDB.getWeek(calendar);
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%20s   %-23s%s\n", "", "Daily Tasks", "EP"));
        List<DailyTaskWithDate> weekend = new ArrayList<>();
        for (DailyTaskWithDate day : week) {
            if (day == null) {
                if (!weekend.isEmpty()) {
                    addWeekendInfo(text, weekend);
                }
                text.append("This date is currently not available\n\n");
                continue;
            }
            if (day.isWeekend()) {
                weekend.add(day);
                continue;
            } else if (!weekend.isEmpty()) {
                // add the weekend info
                addWeekendInfo(text, weekend);
            }
            text.append("\n");
            if (day.isToday(now)) {
                text.append(String.format("%s %d %s\n### Today ###\n", day.dayOfWeek, day.dayOfMonth, day.month));
            } else {
                text.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
            }
            for (String task : day.tasks) {
                text.append(String.format("%20s   %-23s%s\n", "", task, getTaskScore(task)));
            }
        }
        // deal with weekend
        addWeekendInfo(text, weekend);

        text.append("```");
        return text.toString();
    }

    private String getTaskScore(String taskName) {
        for (Task task : tasks) {
            if (task.taskName.equalsIgnoreCase(taskName)) {
                return String.valueOf(task.ep);
            }
        }
        return "???";
    }

    private void addWeekendInfo(StringBuilder messageText, List<DailyTaskWithDate> weekend) {
        Calendar now = Calendar.getInstance();
        if (weekend.isEmpty()) return;
        messageText.append("\n");
        if (weekend.size() == 1) {
            DailyTaskWithDate day = weekend.get(0);
            if (day.isToday(now))
                messageText.append(String.format("%s %d %s\n### Today ###\n", day.dayOfWeek, day.dayOfMonth, day.month));
            else
                messageText.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
            for (String task : day.tasks) {
                messageText.append(String.format("%20s   %-23s%s\n", "", task, getTaskScore(task)));
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
        for (DailyTaskWithDate day : firstMonthWeekend) {
            messageText.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
        }
        for (DailyTaskWithDate day : firstMonthWeekend) {
            for (String task : day.tasks) {
                messageText.append(String.format("%20s   %-23s%s\n", "", task, getTaskScore(task)));
            }
        }
        if (lastMonthWeekend.isEmpty())
            return;
        if (lastMonthWeekend.size() == 1) {
            DailyTaskWithDate day = lastMonthWeekend.get(0);
            messageText.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
            for (String task : day.tasks) {
                messageText.append(String.format("%20s   %-23s%s\n", "", task, getTaskScore(task)));
            }
            return;
        }
        for (DailyTaskWithDate day : lastMonthWeekend) {
            messageText.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
        }
        for (DailyTaskWithDate day : lastMonthWeekend) {
            for (String task : day.tasks) {
                messageText.append(String.format("%20s   %-23s%s\n", "", task, getTaskScore(task)));
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
}
