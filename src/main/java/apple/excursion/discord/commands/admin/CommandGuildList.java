package apple.excursion.discord.commands.admin;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandGuildList implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<Guild> guilds = DiscordBot.client.getGuilds();
        StringBuilder text = new StringBuilder();
        text.append("A list of guilds I'm in is as follows:\n");
        for (Guild guild : guilds) {
            text.append(guild.getName());
            Member owner = guild.retrieveOwner().complete();
            if (owner != null) {
                text.append(" - Owner: ").append(owner.getUser().getAsTag());
            }
            text.append("\n");
        }
        event.getChannel().sendMessage(text.toString()).queue();
    }
}
