package apple.excursion.discord.commands;

import apple.excursion.discord.commands.admin.*;
import apple.excursion.discord.commands.admin.apple.CommandDatabaseImport;
import apple.excursion.discord.commands.admin.apple.CommandExecuteSql;
import apple.excursion.discord.commands.admin.apple.CommandSheetImport;
import apple.excursion.discord.commands.admin.apple.CommandSpeedTest;
import apple.excursion.discord.commands.admin.blacklist.CommandBanAddDaily;
import apple.excursion.discord.commands.admin.blacklist.CommandBanRemoveDaily;
import apple.excursion.discord.commands.admin.reviewer.CommandAddReviewer;
import apple.excursion.discord.commands.admin.reviewer.CommandRemoveReviewer;
import apple.excursion.discord.commands.admin.reviewer.CommandTransfer;
import apple.excursion.discord.commands.admin.reviewer.CommandListPending;
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
    DATABASE_IMPORT(Collections.singletonList("database_import"), "Imports the data from the database to make sure everything is synced",
            "", new CommandDatabaseImport()),
    SHEET_VERIFICATION(Collections.singletonList("sheet_verify"), "Verifies the task scores are the same on playerStats and tasks",
            "", new CommandSheetVerification()),
    FAKE_SUBMIT(Collections.singletonList("fake_submit"), "Submits something as if another player submitted it and it was accepted",
            "<submitterId> <points> <submitterName> <taskCategory> <taskName>", new CommandFakeSubmit()),
    BAN_ADD(Collections.singletonList("bl_add"), "Adds a ban for the list of daily tasks to be chosen",
            "<dailyTask>", new CommandBanAddDaily()),
    BAN_REMOVE(Collections.singletonList("bl_remove"), "Removes a ban for the list of daily tasks to be chosen",
            "dailyTask", new CommandBanRemoveDaily()),
    SQL(Collections.singletonList("sql"), "executes sql",
            "<sql>", new CommandExecuteSql()),
    TRANSFER(Collections.singletonList("transfer"), "Transfers a task name to a new task name (the apostrophes are important", "'[old_name]' '[new name]'", new CommandTransfer()),
    GUILD_LIST(Collections.singletonList("gdiscord list"), "lists all the discords Yin is in", "", new CommandGuildList()),
    SPEED_TEST(Collections.singletonList("speedtest"), "does a speed test", "", new CommandSpeedTest()),
    LIST_PENDING(Collections.singletonList("pending"), "lists all pending submissions", "", new CommandListPending());

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
