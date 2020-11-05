package apple.excursion.discord.commands.admin.reviewer;

import apple.excursion.database.queries.UpdateDB;
import apple.excursion.discord.commands.CommandsAdmin;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;

public class CommandTransfer implements DoCommand {

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        char[] content = event.getMessage().getContentDisplay().toCharArray();
        StringBuilder oldNameBuilder = new StringBuilder();
        StringBuilder newNameBuilder = new StringBuilder();
        boolean onFirst = false;
        boolean onMiddle = false;
        boolean onSecond = false;
        boolean done = false;
        for (char c : content) {
            if (c == '"') {
                if (onFirst) {
                    onFirst = false;
                    onMiddle = true;
                } else if (onMiddle) {
                    onMiddle = false;
                    onSecond = true;
                } else if (onSecond) {
                    onSecond = false;
                    done = true;
                } else {
                    onFirst = true;
                }
            } else if (onFirst) {
                oldNameBuilder.append(c);
            } else if (onSecond) {
                newNameBuilder.append(c);
            }
        }
        if (oldNameBuilder.length() == 0 || newNameBuilder.length() == 0 || !done) {
            event.getChannel().sendMessage(CommandsAdmin.TRANSFER.getHelpMessage()).queue();
            return;
        }
        String oldName = oldNameBuilder.toString();
        String newName = newNameBuilder.toString();
        try {
            UpdateDB.updateTaskName(oldName,newName);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            event.getChannel().sendMessage("There was an SQLException updating the taskName").queue();
            return;
        }
        event.getChannel().sendMessage(String.format("Task name \"%s\" has been changed to \"%s\"", oldName, newName)).queue();

    }
}
