package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.leaderboard.LeaderBoard;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.LeaderBoardMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        LeaderBoard.update();
        AllReactables.add(new LeaderBoardMessage(event.getChannel()));
    }
}
