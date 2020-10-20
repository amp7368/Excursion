package apple.excursion.discord.commands.general.self;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerLeaderboardEntry;
import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.self.ProfileMessage;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

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
                // it's possible that the person exists, but just hasn't submitted anything
                List<PlayerHeader> playerHeaders;
                try {
                    playerHeaders = GetDB.getPlayerHeaders();
                } catch (SQLException throwables) {
                    throwables.printStackTrace(); //todo
                    return;
                }
                PlayerHeader player = null;
                for (PlayerHeader playerHeader : playerHeaders) {
                    if (playerHeader.name.toLowerCase().contains(nameToGet.toLowerCase())) {
                        player = playerHeader;
                        break;
                    }
                }
                if (player == null) {
                    // quit with an error message
                    event.getChannel().sendMessage(String.format("Nobody's name contains '%s'.", nameToGet)).queue();
                    return;
                } else {
                    // we have the correct player header
                    playerLeaderboardEntry = leaderboard.add(player);
                }
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
            long discordId = event.getAuthor().getIdLong();
            playerLeaderboardEntry = leaderboard.get(discordId);
            if (playerLeaderboardEntry == null) {
                // it's possible that the person exists, but just hasn't submitted anything
                PlayerHeader player = getPlayerFromHeaders(discordId);
                if (player == null) {
                    ColoredName coloredName = GetColoredName.get(discordId);
                    String playerName;
                    if ((playerName = coloredName.getName()) == null) {
                        Member member = event.getMember();
                        if (member == null) playerName = event.getAuthor().getName();
                        else playerName = member.getEffectiveName();
                    }
                    try {
                        InsertDB.insertPlayer(new Pair<>(discordId, playerName), null, null);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();//todo
                        return;
                    }
                    player = getPlayerFromHeaders(discordId);
                    if (player == null) {
                        // return and throw errors
                        return;
                    }
                }
                // we have the correct player header
                playerLeaderboardEntry = leaderboard.add(player);
            }
        }
        // we have the player

        GuildLeaderboardEntry guild;
        if (playerLeaderboardEntry.guildIsDefault()) {
            guild = null;
        } else {
            try {
                LeaderboardOfGuilds guildLeaderboard = GetDB.getGuildLeaderboard();
                guild = guildLeaderboard.get(playerLeaderboardEntry.getGuildTag(), playerLeaderboardEntry.getGuildName());
                if (guild == null) {
                    // the guild for sure doesn't have a score. check if they exist or just have no score
                    List<GuildHeader> guildList = GetDB.getGuildNameList();
                    for (GuildHeader header : guildList) {
                        if (header.tag.equals(playerLeaderboardEntry.guildTag)) {
                            guild = guildLeaderboard.add(header);
                            break;
                        }
                    }
                    // this guild doesn't exist so leave it as null
                }
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
        new ProfileMessage(playerLeaderboardEntry, player, guild, coloredName, event.getChannel());

    }

    @Nullable
    private PlayerHeader getPlayerFromHeaders(long discordId) {
        List<PlayerHeader> playerHeaders;
        try {
            playerHeaders = GetDB.getPlayerHeaders();
        } catch (SQLException throwables) {
            throwables.printStackTrace(); //todo
            return null;
        }
        PlayerHeader player = null;
        for (PlayerHeader playerHeader : playerHeaders) {
            if (playerHeader.id == discordId) {
                player = playerHeader;
                break;
            }
        }
        return player;
    }
}