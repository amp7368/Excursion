package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DetermineArguments;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.history.GuildHistoryMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CommandGuildHistory implements DoCommand {

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> contentSplit = new ArrayList<>(Arrays.asList(event.getMessage().getContentStripped().split(" ")));
        contentSplit.remove(0);
        DetermineArguments.ArgumentInt months = DetermineArguments.determineInt("-m", contentSplit);
        DetermineArguments.ArgumentInt weeks = DetermineArguments.determineInt("-w", contentSplit);
        DetermineArguments.ArgumentInt days = DetermineArguments.determineInt("-d", contentSplit);

        // todo deal with the guild side of this command

        if (months.exists) {
            // deal with history in months
            if (months.hasValue) {
                // deal with history in this month intervals
                new GuildHistoryMessage(event.getChannel(), months.value, Calendar.MONTH);
            } else {
                new GuildHistoryMessage(event.getChannel(), 1,Calendar.MONTH);
            }
        } else if (weeks.exists) {
            // deal with history in weeks
            if (weeks.hasValue) {
                // deal with history in this month intervals
                new GuildHistoryMessage(event.getChannel(), weeks.value, Calendar.WEEK_OF_YEAR);
            } else {
                new GuildHistoryMessage(event.getChannel(), 1,Calendar.WEEK_OF_YEAR);
            }
        } else if (days.exists) {
            // deal with history in days
            if (days.hasValue) {
                // deal with history in this month intervals
                new GuildHistoryMessage(event.getChannel(), days.value, Calendar.DAY_OF_YEAR);
            } else {
                new GuildHistoryMessage(event.getChannel(), 1,Calendar.DAY_OF_YEAR);
            }
        }


    }
}
