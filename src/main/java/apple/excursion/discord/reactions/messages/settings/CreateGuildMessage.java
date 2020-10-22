package apple.excursion.discord.reactions.messages.settings;

import apple.excursion.database.queries.UpdateDB;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.io.IOException;
import java.sql.SQLException;

public class CreateGuildMessage implements ReactableMessage {
    private final String guildName;
    private final String guildTag;
    private final long playerId;
    private final String playerName;
    private final Message message;
    private long lastUpdated = System.currentTimeMillis();
    private boolean wasReactedBad = false;

    public CreateGuildMessage(String guildName, String guildTag, long playerId, String playerName, MessageChannel channel) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.playerId = playerId;
        this.playerName = playerName;
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s [%s]", guildName, guildTag));
        embed.setDescription(
                wasReactedBad ? String.format("I was asking %s if they wanted to create this guild.\n", playerName) +
                        "Are you sure you want to create the guild " + String.format("%s [%s]?", guildName, guildTag) :
                        "Are you sure you want to create the guild " + String.format("%s [%s]?", guildName, guildTag));
        return embed.build();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case ACCEPT:
                if (user.getIdLong() == playerId) {
                    // make the guild
                    AllReactables.remove(getId());
                    try {
                        UpdateDB.createGuild(guildName, guildTag);
                        UpdateDB.updateGuild(guildName, guildTag, playerId, playerName);
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle(String.format("%s [%s]", guildName, guildTag));
                        embed.setDescription(String.format("%s [%s] has been created", guildName, guildTag));
                        event.getChannel().sendMessage(embed.build()).queue();
                        message.clearReactions().queue();
                    } catch (SQLException throwables) {
                        event.getChannel().sendMessage("There was an SQL Exception creating this guild").queue();
                        return;
                    } catch (IOException throwables) {
                        event.getChannel().sendMessage("There was an IOException creating this guild. Please try again even if you're in the correct guild.").queue();
                        return;
                    }
                } else {
                    lastUpdated = System.currentTimeMillis();
                    wasReactedBad = true;
                    message.editMessage(makeMessage()).queue();
                }
                break;
            case REJECT:

                if (user.getIdLong() == playerId) {
                    // make the guild
                    AllReactables.remove(getId());
                    message.clearReactions().queue();
                } else {
                    lastUpdated = System.currentTimeMillis();
                    wasReactedBad = true;
                    message.editMessage(makeMessage()).queue();
                }
                break;
        }
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
