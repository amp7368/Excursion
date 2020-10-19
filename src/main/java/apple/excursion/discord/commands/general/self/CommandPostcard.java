package apple.excursion.discord.commands.general.self;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.messages.self.PostcardListMessage;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.PostcardDisplay;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandPostcard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] contentSplit = event.getMessage().getContentStripped().split(" ");
        if (contentSplit.length == 1) {
            new PostcardListMessage(event.getChannel());
            return;
        }
        StringBuilder taskNameBuilder = new StringBuilder();
        taskNameBuilder.append(contentSplit[1]);
        for (int i = 2; i < contentSplit.length; i++) {
            taskNameBuilder.append(" ");
            taskNameBuilder.append(contentSplit[i]);
        }
        final String taskName = taskNameBuilder.toString();
        String taskNameLowercase = taskName.toLowerCase();
        List<Task> tasks = SheetsTasks.getTasks();
        List<Task> answers = new ArrayList<>();
        for (Task task : tasks) {
            if (task.taskName.toLowerCase().contains(taskNameLowercase))
                answers.add(task);
        }
        if (answers.isEmpty()) {
            event.getChannel().sendMessage(String.format("There is no task that contains \"%s\"", taskName)).queue();
        } else if (answers.size() == 1) {
            event.getChannel().sendMessage(PostcardDisplay.getMessage(answers.get(0))).queue();
        } else {
            new PostcardListMessage(event.getChannel(), answers);
        }
    }
}
