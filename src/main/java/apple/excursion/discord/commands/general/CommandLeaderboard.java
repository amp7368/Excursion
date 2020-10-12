package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.LeaderboardMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        new LeaderboardMessage(event.getChannel());
    }
}
