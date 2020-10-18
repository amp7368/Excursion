package apple.excursion.discord.commands.general;

import apple.excursion.database.GetDB;
import apple.excursion.database.UpdateDB;
import apple.excursion.database.objects.GuildData;
import apple.excursion.discord.commands.Commands;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.CreateGuildMessage;
import apple.excursion.sheets.SheetsPlayerStats;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandGuild implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] contentSplit = event.getMessage().getContentStripped().split(" ");
        if (contentSplit.length < 2) {
            event.getChannel().sendMessage(Commands.GUILD.getUsageMessage()).queue();
            return;
        }
        List<GuildData> guilds=null;
//        try {
//            guilds = GetDB.getGuildList(); //todo change this to only get guildNames
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//            return;
//        }
        GuildData match = null;
        final String guildTag = contentSplit[1];
        for (GuildData guild : guilds) {
            if (guild.tag.equals(guildTag)) {
                match = guild;
                break;
            }
        }
        final Member member = event.getMember();
        if (member == null) return;
        final String playerName = member.getEffectiveName();
        final long playerId = event.getAuthor().getIdLong();
        if (match == null) {
            if (contentSplit.length < 3) {
                event.getChannel().sendMessage(
                        String.format("'%s' is not a valid guild tag\n" +
                                        "If you're trying to create a guild, add the [guild_name] to the end of the command",
                                guildTag)).queue();
                return;
            }
            List<String> guildNameSplit = new ArrayList<>(Arrays.asList(contentSplit));
            guildNameSplit.remove(0);
            guildNameSplit.remove(0);
            String guildName = String.join(" ", guildNameSplit);
            // ask the player if they want to create the guild
            new CreateGuildMessage(guildName, guildTag, playerId, playerName, event.getChannel());
            return;
        }
        // change the player's guild
        try {
            UpdateDB.updateGuild(match.name, match.tag, playerId, playerName);
            SheetsPlayerStats.updateGuild(match.name, match.tag, playerId, playerName);
            event.getChannel().sendMessage(String.format("You are now in **%s [%s]**", match.name, match.tag)).queue();
        } catch (SQLException throwables) {
            //todo deal with errors
            throwables.printStackTrace();
        } catch (IOException e) {
            // todo deal with errors
            e.printStackTrace();
        }
    }
}
