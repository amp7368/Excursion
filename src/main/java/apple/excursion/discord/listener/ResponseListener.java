package apple.excursion.discord.listener;

import apple.excursion.discord.reactions.messages.ShoutConfirmationMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.stream.Collectors;

public class ResponseListener implements ChannelListener {
    private final MessageChannel channel;
    private final long lastUpdated = System.currentTimeMillis();
    private final List<Pair<Long, String>> responseRecipients;

    public ResponseListener(MessageChannel channel, List<Pair<Long, String>> responseRecipients) {
        this.channel = channel;
        this.responseRecipients = responseRecipients;
        AllChannelListeners.add(this);
        channel.sendMessage(String.format("Type the message you would like to send to *%s*.\n**(\"cancel\" to cancel)**", responseRecipients.stream().map(Pair::getValue).collect(Collectors.joining(",and ")))).queue();
    }

    @Override
    public void dealWithMessage(MessageReceivedEvent event) {
        AllChannelListeners.remove(channel.getIdLong());
        // we always remove the listener after the first message
        if (!event.getMessage().getContentStripped().equalsIgnoreCase("cancel"))
            new ShoutConfirmationMessage(channel, responseRecipients, event.getMessage().getContentStripped());
        else
            channel.sendMessage("You have canceled sending the messages").queue();
    }

    @Override
    public long getId() {
        return channel.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
