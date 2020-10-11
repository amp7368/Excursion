package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.leaderboard.Leaderboard;
import apple.excursion.discord.reactions.GuildLeaderboardMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandGuildLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        Leaderboard.update();
        new GuildLeaderboardMessage(event.getChannel());
    }
}
