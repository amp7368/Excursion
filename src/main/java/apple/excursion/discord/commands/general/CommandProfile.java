package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.reactions.messages.ProfileMessage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandProfile implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        AllProfiles.update();
        new ProfileMessage(event);
    }
}
