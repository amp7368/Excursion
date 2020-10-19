package apple.excursion.discord.commands.admin;

import apple.excursion.database.GetDB;
import apple.excursion.database.SyncDB;
import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.sheets.profiles.AllProfiles;
import apple.excursion.sheets.profiles.Profile;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CommandSheetImport implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<Profile> sheetData;
        List<PlayerHeader> databasePlayers;
        List<GuildHeader> databaseGuilds;
        try {
            sheetData = AllProfiles.getProfiles();
            databasePlayers = GetDB.getPlayerHeaders();
            databaseGuilds = GetDB.getGuildNameList();
        } catch (IOException e) {
            event.getChannel().sendMessage("There was an IOException importing everything from the sheet").queue();
            return;
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There was an SQLException getting the data from the database").queue();
            return;
        }
        try {
            List<String> logs = SyncDB.sync(sheetData, databasePlayers, databaseGuilds);
            PrivateChannel dms = DiscordBot.client.getUserById(253646208084475904L).openPrivateChannel().complete();
            StringBuilder builder = new StringBuilder();
            for (String log : logs) {
                if (log.length() + builder.length() > 1999) {
                    dms.sendMessage(builder.toString()).queue();
                    builder = new StringBuilder();
                }
                builder.append(log);
                builder.append('\n');
            }
            if (builder.length() != 0)
                dms.sendMessage(builder.toString()).queue();
            event.getChannel().sendMessage(logs.size() + " logs were sent to appleptr16").queue();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            event.getChannel().sendMessage("There was an SQLException importing the data into the database").queue();
        }

    }
}
