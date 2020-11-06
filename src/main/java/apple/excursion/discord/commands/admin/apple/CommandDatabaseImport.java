package apple.excursion.discord.commands.admin.apple;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.queries.SyncDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.sheets.profiles.AllProfiles;
import apple.excursion.sheets.profiles.Profile;
import apple.excursion.utils.Pair;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static apple.excursion.discord.commands.admin.apple.CommandSheetImport.WORKING_EMOJI;

public class CommandDatabaseImport implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        Set<Profile> sheetData;
        List<PlayerHeader> databasePlayers;
        List<PlayerData> playerData;
        event.getMessage().addReaction(WORKING_EMOJI).queue();
        List<TaskSimple> tasksList;
        try {
            sheetData = AllProfiles.getProfiles();
            tasksList = SheetsPlayerStats.getTasks();
            databasePlayers = GetDB.getPlayerHeaders();
            playerData = new ArrayList<>();
            for (PlayerHeader header : databasePlayers)
                playerData.add(GetDB.getPlayerData(new Pair<>(header.id, header.name)));
        } catch (IOException e) {
            event.getChannel().sendMessage("There was an IOException importing everything from the sheet").queue();
            return;
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There was an SQLException getting the data from the database").queue();
            return;
        }
        try {
            List<String> logs = SyncDB.syncUsingDB(sheetData, databasePlayers, playerData,tasksList);
            SendLogs.sendLogs(logs);
            event.getChannel().sendMessage(logs.size() + " logs were sent to appleptr16").queue();
        } catch (IOException e) {
            e.printStackTrace();
            event.getChannel().sendMessage("There was an IOException importing the data into the database").queue();
        }
        event.getMessage().removeReaction(WORKING_EMOJI, DiscordBot.client.getSelfUser()).queue();
    }
}
