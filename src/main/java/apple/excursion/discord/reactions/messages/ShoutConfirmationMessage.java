package apple.excursion.discord.reactions.messages;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;
import java.util.stream.Collectors;

public class ShoutConfirmationMessage implements ReactableMessage {
    private final Message message;
    private final List<Pair<Long, String>> responseRecipients;
    private final MessageEmbed messageToSend;

    private long lastUpdated = System.currentTimeMillis();

    public ShoutConfirmationMessage(MessageChannel channel, List<Pair<Long, String>> responseRecipients, String content) {
        this.responseRecipients = responseRecipients;
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("You've recieved a message");
        embed.setDescription(content);
        this.messageToSend = embed.build();
        this.message = channel.sendMessage(makeMessage(responseRecipients, content)).complete();
        this.message.addReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private static MessageEmbed makeMessage(List<Pair<Long, String>> responseRecipients, String content) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Message Confirmation");
        embed.setDescription(String.format("Are you sure you want to send the following to %s\n\n%s",
                responseRecipients.stream().map(Pair::getValue).collect(Collectors.joining(",and ")),
                content
        ));
        return embed.build();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        switch (reactable) {
            case ACCEPT:
                // send the messages
                for (Pair<Long, String> recipient : responseRecipients) {
                    DiscordBot.client.retrieveUserById(recipient.getKey()).queue(user -> {
                        if (user == null || user.isBot()) return;
                        user.openPrivateChannel().complete().sendMessage(messageToSend).queue();
                    }, failure -> event.getChannel().sendMessage("I couldn't send that message to " + recipient.getValue()).queue());
                }
                message.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                message.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
                AllReactables.remove(message.getIdLong());
                break;
            case REJECT:
                message.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                message.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
                AllReactables.remove(message.getIdLong());
                break;
        }
        lastUpdated = System.currentTimeMillis();
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
