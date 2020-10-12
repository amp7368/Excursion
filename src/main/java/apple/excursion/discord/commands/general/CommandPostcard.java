package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.PostcardListMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandPostcard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] contentSplit = event.getMessage().getContentStripped().split(" ");
        if (contentSplit.length == 1) {
            new PostcardListMessage(event.getChannel());
        }
    }
}
