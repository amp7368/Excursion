package apple.excursion.discord.commands.cross_chat;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.cross_chat.CrossChat;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRemoveCrossChat implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        long serverId = event.getGuild().getIdLong();
        CrossChat.remove(serverId,event.getChannel());
    }
}
