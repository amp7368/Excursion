package apple.excursion.discord.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface DoCommand {
    void dealWithCommand(MessageReceivedEvent eventObject);
}
