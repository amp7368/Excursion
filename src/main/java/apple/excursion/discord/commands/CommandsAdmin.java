package apple.excursion.discord.commands;

import apple.excursion.discord.commands.admin.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Collections;
import java.util.List;

import static apple.excursion.discord.DiscordBot.PREFIX;


public enum CommandsAdmin {
    ADD_REVIEWER(Collections.singletonList("add_reviewer"), "Adds a reviewer to the list of reviewers",
            "[@mention]", new CommandAddReviewer()),
    REMOVE_REVIEWER(Collections.singletonList("remove_reviewer"), "Removes a reviewer from the list of reviewers",
            "[@mention]", new CommandRemoveReviewer()),
    SHEET_IMPORT(Collections.singletonList("sheet_import"), "Imports the data from the google sheet to make sure everything is synced",
            "", new CommandSheetImport()),
    FAKE_SUBMIT(Collections.singletonList("fake_submit"), "Submits something as if another player submitted it and it was accepted",
            "<submitterId> <points> <submitterName> <taskCategory> <taskName>", new CommandFakeSubmit()),
    SQL(Collections.singletonList("sql"), "executes sql",
            "<sql>", new CommandExecuteSql());

    private final List<String> commandNames;
    private final String helpMessage;
    private final String usageMessage;
    private final DoCommand run;

    CommandsAdmin(List<String> commandNames, String helpMessage, String usageMessage, DoCommand command) {
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
