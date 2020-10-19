package apple.excursion.discord.commands.general;

import apple.excursion.database.GetDB;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerLeaderboardEntry;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.ProfileMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.List;

public class CommandProfile implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        final String[] eventContentSplitOnce = event.getMessage().getContentStripped().split(" ", 2);
        PlayerLeaderboardEntry playerLeaderboardEntry;
        final String nameToGet;
        if (eventContentSplitOnce.length > 1) {
            nameToGet = eventContentSplitOnce[1];
            List<PlayerLeaderboardEntry> players;
            try {
                players = GetDB.getPlayerLeaderboard().getPlayersWithName(nameToGet);
            } catch (SQLException throwables) {
                throwables.printStackTrace(); //todo
                return;
            }
            final int playersWithNameLength = players.size();
            if (playersWithNameLength == 0) {
                // quit with an error message
                event.getChannel().sendMessage(String.format("Nobody's name contains '%s'.", nameToGet)).queue();
                return;
            } else if (playersWithNameLength == 1) {
                // we found the person
                playerLeaderboardEntry = players.get(0);
            } else {
                // ask the user to narrow their search
                //todo what if multiple ppl have the same name
                event.getChannel().sendMessage(String.format("There are %d people that have '%s' in their name.", playersWithNameLength, nameToGet)).queue();
                return;
            }
        } else {
            try {
                playerLeaderboardEntry = GetDB.getPlayerLeaderboard().get(event.getAuthor().getIdLong());
            } catch (SQLException throwables) {
                throwables.printStackTrace(); //todo
                return;
            }
        }
        // we have the player

        GuildLeaderboardEntry guild;
        if (playerLeaderboardEntry.guildIsDefault()) {
            guild = null;
        } else {
            try {
                guild = GetDB.getGuildLeaderboard().get(playerLeaderboardEntry.getGuildTag(), playerLeaderboardEntry.getGuildName());
            } catch (SQLException throwables) {
                throwables.printStackTrace(); //todo
                return;
            }
        }
        // we have the player's guild or null
        PlayerData player;
        try {
            player = GetDB.getPlayerData(new Pair<>(playerLeaderboardEntry.getId(), playerLeaderboardEntry.playerName), -1); //get all of them
        } catch (SQLException throwables) {
            throwables.printStackTrace();//todo
            return;
        }
        new ProfileMessage(playerLeaderboardEntry, player, guild, event.getChannel());

    }
}