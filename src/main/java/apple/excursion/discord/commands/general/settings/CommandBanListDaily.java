package apple.excursion.discord.commands.general.settings;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.DailyBans;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandBanListDaily implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> bans = DailyBans.getBans();
        if (bans.isEmpty())
            event.getChannel().sendMessage("There are currently no blacklisted dailies (tasks that are banned from becoming daily tasks)").queue();
        else
            event.getChannel().sendMessage("The tasks that will not become daily tasks is as follows: \n" + String.join("\n", bans)).queue();
    }
}
