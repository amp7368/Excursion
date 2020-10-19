package apple.excursion.discord.commands.general.leaderboard;

import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.discord.reactions.messages.leaderboard.GuildLeaderboardMessage;
import apple.excursion.discord.reactions.messages.leaderboard.GuildProfileMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                //todo deal with error
                throwables.printStackTrace();
            }
            return;
        }
        contentSplit.remove(0);
        String inputAsGuildTag = contentSplit.get(0);
        String inputAsGuildName = String.join(" ", contentSplit);

        LeaderboardOfGuilds leaderboard;
        try {
            leaderboard = GetDB.getGuildLeaderboard();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }
        GuildLeaderboardEntry matchedGuild = leaderboard.get(inputAsGuildTag, inputAsGuildName);

        if (matchedGuild == null) {
            event.getChannel().sendMessage(String.format("There is no guild with tag **[%s]** nor with name **%s**", inputAsGuildTag, inputAsGuildName)).queue();
            return;
        }
        // we have the correct guild
        List<PlayerData> playersInGuild;
        try {
            playersInGuild = GetDB.getPlayersInGuild(matchedGuild.guildTag, -1, -1);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }
        List<OldSubmission> submissions;
        try {
            submissions = GetDB.getGuildSubmissions(matchedGuild.guildTag, -1, -1);
        } catch (SQLException throwables) {
            throwables.printStackTrace(); //todo fix
            return;
        }
        new GuildProfileMessage(submissions, matchedGuild, playersInGuild, event.getChannel());
    }
}
