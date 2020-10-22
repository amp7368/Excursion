package apple.excursion.discord.commands.general.benchmark;

import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.discord.commands.DetermineArguments;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.benchmark.history.SpecificGuildHistoryMessage;
import apple.excursion.discord.reactions.messages.benchmark.history.GuildHistoryMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
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

        if (contentSplit.isEmpty()) {
            if (months.exists) {
                // deal with history in months
                if (months.hasValue) {
                    // deal with history in this month intervals
                    new GuildHistoryMessage(event.getChannel(), months.value, Calendar.MONTH);
                } else {
                    new GuildHistoryMessage(event.getChannel(), 1, Calendar.MONTH);
                }
            } else if (weeks.exists) {
                // deal with history in weeks
                if (weeks.hasValue) {
                    // deal with history in this month intervals
                    new GuildHistoryMessage(event.getChannel(), weeks.value, Calendar.WEEK_OF_YEAR);
                } else {
                    new GuildHistoryMessage(event.getChannel(), 1, Calendar.WEEK_OF_YEAR);
                }
            } else if (days.exists) {
                // deal with history in days
                if (days.hasValue) {
                    // deal with history in this month intervals
                    new GuildHistoryMessage(event.getChannel(), days.value, Calendar.DAY_OF_YEAR);
                } else {
                    new GuildHistoryMessage(event.getChannel(), 1, Calendar.DAY_OF_YEAR);
                }
            } else {
                new GuildHistoryMessage(event.getChannel(), 1, Calendar.MONTH);
            }
        } else {
            // try to find the guild
            List<GuildHeader> guildHeaders;
            try {
                guildHeaders = GetDB.getGuildNameList();
            } catch (SQLException throwables) {
                event.getChannel().sendMessage("There has been an SQLException trying to get all the guild names.").queue();
                return;
            }
            final String guildTag = contentSplit.get(0);
            contentSplit.remove(0);
            String guildName = String.join(" ", contentSplit);
            GuildHeader match = null;
            for (GuildHeader guild : guildHeaders) {
                if (guild.tag.equals(guildTag) || guildName.equals(guild.name)) {
                    match = guild;
                    break;
                }
            }
            if (match == null) {
                event.getChannel().sendMessage(
                        String.format("'%s' is not a valid guild tag\n" +
                                        "If you're trying to create a guild, add the [guild_name] to the end of the command",
                                guildTag)).queue();
                return;
            }
            // deal with the guildHistoryMessage
            if (months.exists) {
                // deal with history in months
                if (months.hasValue) {
                    // deal with history in this month intervals
                    new SpecificGuildHistoryMessage(event.getChannel(), months.value, Calendar.MONTH, match.tag, match.name);
                } else {
                    new SpecificGuildHistoryMessage(event.getChannel(), 1, Calendar.MONTH, match.tag, match.name);
                }
            } else if (weeks.exists) {
                // deal with history in weeks
                if (weeks.hasValue) {
                    // deal with history in this month intervals
                    new SpecificGuildHistoryMessage(event.getChannel(), weeks.value, Calendar.WEEK_OF_YEAR, match.tag, match.name);
                } else {
                    new SpecificGuildHistoryMessage(event.getChannel(), 1, Calendar.WEEK_OF_YEAR, match.tag, match.name);
                }
            } else if (days.exists) {
                // deal with history in days
                if (days.hasValue) {
                    // deal with history in this month intervals
                    new SpecificGuildHistoryMessage(event.getChannel(), days.value, Calendar.DAY_OF_YEAR, match.tag, match.name);
                } else {
                    new SpecificGuildHistoryMessage(event.getChannel(), 1, Calendar.DAY_OF_YEAR, match.tag, match.name);
                }
            } else {
                new SpecificGuildHistoryMessage(event.getChannel(), 1, Calendar.MONTH, match.tag, match.name);
            }
        }
    }
}
