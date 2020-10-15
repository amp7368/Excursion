package apple.excursion.discord.commands.general;

import apple.excursion.database.GetDB;
import apple.excursion.database.objects.GuildData;
import apple.excursion.database.objects.PlayerData;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.answers.GuildLeaderboardProfile;
import apple.excursion.discord.reactions.messages.GuildLeaderboardMessage;
import apple.excursion.discord.reactions.messages.GuildProfileMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
            AllProfiles.update();
            new GuildLeaderboardMessage(event.getChannel());
            return;
        }
        contentSplit.remove(0);
        String inputAsGuildTag = contentSplit.get(0);
        String inputAsGuildName = String.join(" ", contentSplit);

        List<GuildData> guilds;
        try {
            guilds = GetDB.getGuildList();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }
        GuildData matchedGuild = null;
        for (GuildData guild : guilds) {
            if (guild.tag.equals(inputAsGuildTag)) {
                matchedGuild = guild;
                break;
            }
        }
        if (matchedGuild == null) {
            for (GuildData guild : guilds) {
                if (guild.name.equals(inputAsGuildName)) {
                    matchedGuild = guild;
                    break;
                }
            }
        }
        if (matchedGuild == null) {
            event.getChannel().sendMessage(String.format("There is no guild with tag **[%s]** nor with name **%s**", inputAsGuildTag, inputAsGuildName)).queue();
            return;
        }
        // we have the correct guild
        List<PlayerData> playersInGuild;
        try {
            playersInGuild = GetDB.getPlayersInGuild(matchedGuild.tag);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return;
        }
        GuildLeaderboardProfile guildProfile = AllProfiles.getLeaderboardOfGuilds().getGuildProfile(matchedGuild.tag);
        new GuildProfileMessage(matchedGuild,playersInGuild,guildProfile,event.getChannel());

    }
}
