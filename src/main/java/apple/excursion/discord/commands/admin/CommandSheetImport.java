package apple.excursion.discord.commands.admin;

import apple.excursion.database.queries.GetDB;
import apple.excursion.database.queries.SyncDB;
import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.sheets.profiles.AllProfiles;
import apple.excursion.sheets.profiles.Profile;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CommandSheetImport implements DoCommand {

    public static final String WORKING_EMOJI = "\uD83D\uDEE0";

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        Set<Profile> sheetData;
        List<PlayerHeader> databasePlayers;
        List<GuildHeader> databaseGuilds;
        List<PlayerData> playerData;
        try {
            sheetData = AllProfiles.getProfiles();
            databasePlayers = GetDB.getPlayerHeaders();
            databaseGuilds = GetDB.getGuildNameList();
            playerData = new ArrayList<>();
            for (PlayerHeader header : databasePlayers)
                playerData.add(GetDB.getPlayerData(new Pair<>(header.id, header.name), -1));
        } catch (IOException e) {
            event.getChannel().sendMessage("There was an IOException importing everything from the sheet").queue();
            return;
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There was an SQLException getting the data from the database").queue();
            return;
        }
        try {
            event.getMessage().addReaction(WORKING_EMOJI).queue();
            List<String> logs = SyncDB.sync(sheetData, databasePlayers,playerData, databaseGuilds);
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
        event.getMessage().removeReaction(WORKING_EMOJI, DiscordBot.client.getSelfUser()).queue();

    }
}
