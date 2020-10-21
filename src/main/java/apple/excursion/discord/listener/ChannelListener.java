package apple.excursion.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface ChannelListener {
    void dealWithMessage(MessageReceivedEvent event);

    long getId();

    long getLastUpdated();

}
