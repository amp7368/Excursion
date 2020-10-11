package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.Commands;
import apple.excursion.discord.commands.CommandsAdmin;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        StringBuilder helpMessage = new StringBuilder();
        if (CommandSubmit.isReviewer(event.getAuthor())) {
            helpMessage.append("***Admin Commands***\n");
            for (CommandsAdmin command : CommandsAdmin.values()) {
                helpMessage.append(command.getHelpMessage());
                helpMessage.append("\n");
            }
            helpMessage.append("***General Commands***\n");
        }
        for (Commands command : Commands.values()) {
            helpMessage.append(command.getHelpMessage());
            helpMessage.append("\n");
        }
        event.getChannel().sendMessage(helpMessage.toString()).queue();
    }
}
