package apple.excursion.discord.commands.admin;

import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.CommandsAdmin;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.List;


public class CommandExecuteSql implements DoCommand {

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        if(event.getAuthor().getIdLong()!= DiscordBot.APPLEPTR16) return;
        String[] message = event.getMessage().getContentStripped().split(" ", 2);
        if (message.length == 1) {
            event.getChannel().sendMessage(CommandsAdmin.SQL.getUsageMessage()).queue();
            return;
        }
        List<String> response;
        try {
            response = GetDB.executeSql(message[1]);
        } catch (SQLException throwables) {
            event.getChannel().sendMessage(throwables.getMessage()).queue();
            return;
        }
        event.getChannel().sendMessage(String.join("\n", response)).queue();
    }
}
