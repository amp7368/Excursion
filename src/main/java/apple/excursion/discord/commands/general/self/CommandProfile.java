package apple.excursion.discord.commands.general.self;

import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerLeaderboardEntry;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.self.ProfileMessage;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
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
        PlayerLeaderboard leaderboard;
        try {
            leaderboard = GetDB.getPlayerLeaderboard();
        } catch (SQLException throwables) {
            throwables.printStackTrace(); //todo
            return;
        }
        if (eventContentSplitOnce.length > 1) {
            nameToGet = eventContentSplitOnce[1];
            List<PlayerLeaderboardEntry> players;
            players = leaderboard.getPlayersWithName(nameToGet);
            if (players.isEmpty()) {
                List<Long> ids = GetColoredName.get(nameToGet);
                players = leaderboard.getPlayersById(ids);
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
            playerLeaderboardEntry = leaderboard.get(event.getAuthor().getIdLong());
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

        ColoredName coloredName = GetColoredName.get(playerLeaderboardEntry.getId());
        PlayerData player;
        try {
            player = GetDB.getPlayerData(new Pair<>(playerLeaderboardEntry.getId(),
                            coloredName.getName() == null ? playerLeaderboardEntry.playerName : coloredName.getName()),
                    -1); //get all of them
        } catch (SQLException throwables) {
            throwables.printStackTrace();//todo
            return;
        }
        new ProfileMessage(playerLeaderboardEntry, player, guild,coloredName, event.getChannel());

    }
}