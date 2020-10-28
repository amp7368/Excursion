package apple.excursion.discord.commands;

import apple.excursion.discord.commands.general.benchmark.*;
import apple.excursion.discord.commands.general.settings.*;
import apple.excursion.discord.commands.general.postcard.CommandPostcard;
import apple.excursion.discord.commands.general.postcard.CommandSubmit;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static apple.excursion.discord.DiscordBot.PREFIX;


public enum Commands {
    LEADERBOARD(Arrays.asList("leaderboard", "lb"), "Gives an overall leaderboard", "", new CommandLeaderboard()),
    LEADERBOARD_IN_GUILD(Arrays.asList("gleaderboard", "glb"), "Gives a leaderboard for a specific guild or the overall guild leaderboard", "[(optional) tag/name]", new CommandGuildLeaderboard()),
    COMMAND_GUILD_HISTORY(Collections.singletonList("ghistory"), "Gives a history with intervals of the specified time", "[(optional) -m [months]] [(optional) -w [weeks]] [(optional) -d [days]]", new CommandGuildHistory()),
    COMMAND_HISTORY(Collections.singletonList("history"), "Gives a history with intervals of the specified time", "[(optional) -m [months]] [(optional) -w [weeks]] [(optional) -d [days]]", new CommandHistory()),
    PROFILE(Collections.singletonList("profile"), "Gives the profile of the person who entered the command or the profile of the player_name", "[player_name]", new CommandProfile()),
    POSTCARD(Collections.singletonList("postcard"), "Gives a list of postcards or searches for taskName", "[(optional) postcard name)]", new CommandPostcard()),
    CALENDAR(Collections.singletonList("calendar"), "Gives a list of daily tasks", "", new CommandCalendar()),
    SUBMIT(Collections.singletonList("submit"), "Submits the attached evidence to be reviewed", "[url or attach image]", new CommandSubmit()),
    GUILD(Collections.singletonList("guild"), "Change your guild", "[guild tag]", new CommandGuild()),
    TITLE(Collections.singletonList("title"), "Change your rank and title", "[title]", new CommandTitle()),
    BAN_LIST(Collections.singletonList("daily"), "Shows the list of daily bans", "", new CommandBanListDaily()),
    CROSS_CHAT_DELETE(Collections.singletonList("delete"), "Deletes the message corresponding to the id if it's your own", "[id]", new CommandCrossChatDelete()),
    BUG_REPORT(Collections.singletonList("bug"), "Reports the following message (with an optional image) as a bug", "", new CommandBugReport()),
    HELP(Collections.singletonList("help"), "Gives this help message", "", new CommandHelp());

    private final List<String> commandNames;
    private final String helpMessage;
    private final String usageMessage;
    private final DoCommand run;

    Commands(List<String> commandNames, String helpMessage, String usageMessage, DoCommand command) {
        this.commandNames = commandNames;
        this.helpMessage = helpMessage;
        this.usageMessage = usageMessage;
        this.run = command;
    }

    public String getHelpMessage() {
        return String.format("**%s%s %s** - %s", PREFIX, commandNames.get(0), usageMessage, helpMessage);
    }

    public String getUsageMessage() {
        return String.format("Usage - %s%s %s", PREFIX, commandNames.get(0), usageMessage);
    }

    public boolean isCommand(String command) {
        for (String myCommand : commandNames)
            if (command.startsWith(PREFIX + myCommand))
                return true;
        return false;
    }

    public void run(MessageReceivedEvent event) {
        run.dealWithCommand(event);
    }
}
