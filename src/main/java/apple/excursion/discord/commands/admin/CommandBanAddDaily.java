package apple.excursion.discord.commands.admin;

import apple.excursion.database.queries.GetCalendarDB;
import apple.excursion.discord.commands.CommandsAdmin;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.DailyBans;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBanAddDaily implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> content = new ArrayList<>(Arrays.asList(event.getMessage().getContentDisplay().split(" ")));
        content.remove(0);
        if (content.isEmpty()) {
            event.getChannel().sendMessage(CommandsAdmin.BAN_ADD.getUsageMessage()).queue();
            return;
        }
        String daily = String.join(" ", content);
        try {
            DailyBans.addBan(daily);
            GetCalendarDB.blacklist(daily);
            event.getChannel().sendMessage("That ban has been successfully added").queue();
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("There was an error adding that ban").queue();
        }
    }
}
