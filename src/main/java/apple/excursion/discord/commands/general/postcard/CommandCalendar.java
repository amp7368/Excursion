package apple.excursion.discord.commands.general.postcard;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.benchmark.CalendarMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandCalendar implements DoCommand {

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        new CalendarMessage(event.getChannel());
    }
}
