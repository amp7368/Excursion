package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.PageableMessages;
import apple.excursion.discord.data.leaderboard.LeaderBoard;
import apple.excursion.discord.data.leaderboard.LeaderBoardMessage;
import apple.excursion.sheets.LeaderBoardSheet;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLeaderboard implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {


        // this is the leaderboard command
        LeaderBoard.update(LeaderBoardSheet.getLeaderBoard());

        PageableMessages.add(new LeaderBoardMessage(event.getChannel()));
    }
}
