package apple.excursion.discord.commands.admin;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandSheetVerification implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<Task> tasks = SheetsTasks.getTasks();
        List<TaskSimple> taskSimples;
        List<String> logs = new ArrayList<>();
        try {
            taskSimples = SheetsPlayerStats.getTasks();
        } catch (IOException e) {
            event.getChannel().sendMessage("There was an IOException doing that").queue();
            return;
        }
        for (Task task : tasks) {
            TaskSimple match = null;
            for (TaskSimple taskSimple : taskSimples) {
                if (task.name.equalsIgnoreCase(taskSimple.name)) {
                    match = taskSimple;
                    break;
                }
            }
            if (match == null) {
                logs.add(String.format("%s has  no corresponding task on PlayerStats", task.name));
                continue;
            }
            if (task.points != match.points) {
                logs.add(String.format("%s has %d points on Tasks and %d points on PlayerStats", task.name, task.points, match.points));
            }
        }
        for (TaskSimple taskSimple : taskSimples) {
            Task match = null;
            for (Task task : tasks) {
                if (task.name.equalsIgnoreCase(taskSimple.name)) {
                    match = task;
                    break;
                }
            }
            if (match == null) {
                logs.add(String.format("%s has  no corresponding task on PlayerStats", taskSimple.name));
            }
        }
        SendLogs.sendLogs(logs);
    }
}
