package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.DailyTaskListMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCalendar implements DoCommand {

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        new DailyTaskListMessage(event.getChannel());
    }
}
