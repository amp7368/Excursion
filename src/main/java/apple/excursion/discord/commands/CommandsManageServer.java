package apple.excursion.discord.commands;

import apple.excursion.discord.commands.cross_chat.CommandRemoveCrossChat;
import apple.excursion.discord.commands.cross_chat.CommandSetCrossChat;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

import static apple.excursion.discord.DiscordBot.PREFIX;


public enum CommandsManageServer {
    DISCORD_SET(Collections.singletonList("discord remove"), "Sets the current channel to link with everyone else's channel",
            "", new CommandRemoveCrossChat()),
    DISCORD_REMOVE(Collections.singletonList("discord set"), "Removes the current server from linking with discord",
            "", new CommandSetCrossChat());

    private final List<String> commandNames;
    private final String helpMessage;
    private final String usageMessage;
    private final DoCommand run;

    CommandsManageServer(List<String> commandNames, String helpMessage, String usageMessage, DoCommand command) {
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
