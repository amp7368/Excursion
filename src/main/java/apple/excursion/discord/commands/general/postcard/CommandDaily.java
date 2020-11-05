package apple.excursion.discord.commands.general.postcard;

import apple.excursion.database.queries.GetCalendarDB;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.Task;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.DayOfWeek;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static apple.excursion.discord.reactions.messages.benchmark.CalendarMessage.EPOCH_YEAR;

public class CommandDaily implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        Calendar calendar = Calendar.getInstance();
        List<String> tasksToday = GetCalendarDB.getTasksToday(calendar);
        int weeksSinceEpoch = calendar.get(Calendar.WEEK_OF_YEAR) + (calendar.get(Calendar.YEAR) - EPOCH_YEAR) * 365;

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("Week %d\n", weeksSinceEpoch));
        text.append(getDash());
        int originalMonth = calendar.get(Calendar.MONTH);
        int originalDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (dayOfWeek == 0) dayOfWeek = 7; // sunday is sunday
        text.append(String.format("| %-23s| %-4s|\n",
                String.format("%s %d %s",
                        DayOfWeek.of(dayOfWeek).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        originalDayOfMonth,
                        Month.of(originalMonth + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH)),
                ""
        ));
        text.append(getDash());
        text.append(String.format("| %-23s| %-4s|\n", "Today's Dailies", "EP"));
        text.append(getDash());
        for (String taskName : tasksToday) {
            Task task = SheetsTasks.getTaskWithName(taskName);
            if (task == null) {
                text.append("Failed to get task\n");
            } else {
                text.append(String.format("| %-23s| %-4s|\n", task.name, task.points));
            }
        }
        text.append(getDash());
        text.append("\n```");
        event.getChannel().sendMessage(text.toString()).queue();

    }

    private String getDash() {
        return "-".repeat(32) + "\n";
    }
}
