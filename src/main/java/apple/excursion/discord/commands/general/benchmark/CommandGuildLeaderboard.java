package apple.excursion.discord.commands.general.benchmark;

import apple.excursion.database.objects.guild.GuildHeader;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.discord.reactions.messages.benchmark.GuildLeaderboardMessage;
import apple.excursion.discord.reactions.messages.benchmark.GuildProfileMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandGuildLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String content = event.getMessage().getContentStripped();
        List<String> contentSplit = new ArrayList<>(Arrays.asList(content.split(" ")));
        if (contentSplit.size() < 2) {
            try {
                LeaderboardOfGuilds leaderboard = GetDB.getGuildLeaderboard();
                new GuildLeaderboardMessage(event.getChannel(), leaderboard);
            } catch (SQLException throwables) {
                event.getChannel().sendMessage("There has been an SQLException trying to get the guild leaderboard.").queue();
                return;
            }
            return;
        }
        contentSplit.remove(0);
        String inputAsGuildTag = contentSplit.get(0);
        String inputAsGuildName = String.join(" ", contentSplit);

        GuildLeaderboardEntry matchedGuild;
        try {
            LeaderboardOfGuilds guildLeaderboard = GetDB.getGuildLeaderboard();
            matchedGuild = guildLeaderboard.get(inputAsGuildTag, inputAsGuildName);
            if (matchedGuild == null) {
                // the guild for sure doesn't have a score. check if they exist or just have no score
                List<GuildHeader> guildList = GetDB.getGuildNameList();
                for (GuildHeader header : guildList) {
                    if (header.tag.equals(inputAsGuildTag)) {
                        matchedGuild = guildLeaderboard.add(header);
                        break;
                    }
                }
                if (matchedGuild == null) {
                    // try case insensitive
                    for (GuildHeader header : guildList) {
                        if (header.tag.equalsIgnoreCase(inputAsGuildTag)) {
                            matchedGuild = guildLeaderboard.add(header);
                            break;
                        }
                    }
                    if (matchedGuild == null) {
                        // try guild name
                        Pattern pattern = Pattern.compile(".*(" + inputAsGuildName + ").*", Pattern.CASE_INSENSITIVE);
                        for (GuildHeader header : guildList) {
                            if (pattern.matcher(header.name).matches()) {
                                matchedGuild = guildLeaderboard.add(header);
                                break;
                            }
                        }
                    }
                }

                // this guild doesn't exist so leave it as null
            }
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There has been an SQLException getting the guild leaderboard or all the guild names.").queue();
            return;
        }
        if (matchedGuild == null) {
            event.getChannel().sendMessage(String.format("There is no guild with tag **[%s]** nor with name **%s**", inputAsGuildTag, inputAsGuildName)).queue();
            return;
        }
        // we have the correct guild
        List<PlayerData> playersInGuild;
        try {
            playersInGuild = GetDB.getPlayersInGuild(matchedGuild.guildTag, -1, -1);
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There was an SQLException getting the playerLeaderboard in this guild").queue();
            return;
        }
        List<OldSubmission> submissions;
        try {
            submissions = GetDB.getGuildSubmissions(matchedGuild.guildTag, -1, -1);
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There has been an SQLException getting the submissions for the guild.").queue();
            return;
        }
        new GuildProfileMessage(submissions, matchedGuild, playersInGuild, event.getChannel());
    }
}
