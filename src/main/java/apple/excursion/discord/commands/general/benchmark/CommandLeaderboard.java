package apple.excursion.discord.commands.general.benchmark;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.messages.benchmark.LeaderboardMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;

public class CommandLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        try {
            new LeaderboardMessage(event.getChannel());
        } catch (SQLException throwables) {
            throwables.printStackTrace();//todo
        }
    }
}
