package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.Commands;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandLeaderboardInGuild implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String content = event.getMessage().getContentStripped();
        List<String> contentSplit = new ArrayList<>(Arrays.asList(content.split(" ")));
        if (contentSplit.size() < 2) {
            event.getChannel().sendMessage(Commands.LEADERBOARD_IN_GUILD.getUsageMessage()).queue();
            return;
        }
        contentSplit.remove(0);
        event.getChannel().sendMessage("not implemented").queue();
    }
}
