package apple.excursion.discord.commands.general.self;


import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.self.HelpMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        new HelpMessage(event.getChannel());
    }
}
