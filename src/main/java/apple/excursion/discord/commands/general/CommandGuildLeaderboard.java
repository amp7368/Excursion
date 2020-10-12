package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.GuildLeaderboardMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandGuildLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        new GuildLeaderboardMessage(event.getChannel());
    }
}
