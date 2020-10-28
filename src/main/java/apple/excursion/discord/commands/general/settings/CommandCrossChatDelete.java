package apple.excursion.discord.commands.general.settings;

import apple.excursion.database.objects.MessageId;
import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.Commands;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.List;

public class CommandCrossChatDelete implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] content = event.getMessage().getContentDisplay().split(" ");
        if (content.length != 2) {
            event.getChannel().sendMessage(Commands.CROSS_CHAT_DELETE.getUsageMessage()).queue();
            return;
        }
        long messageIdToFind;
        try {
            messageIdToFind = Long.parseLong(content[1]);
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(String.format("'%s' is not a valid id for a message.", content[1])).queue();
            return;
        }
        List<MessageId> messages;
        try {
            messages = GetDB.getCrossChatMessageIds(messageIdToFind, event.getAuthor().getIdLong());
        } catch (SQLException e) {
            event.getChannel().sendMessage(String.format("You don't own any messages of id '%d'", messageIdToFind)).queue();
            return;
        }
        if (messages.isEmpty()) {
            event.getChannel().sendMessage(String.format("You don't own any messages of id '%d'", messageIdToFind)).queue();
            return;
        }
        for (MessageId messageId : messages) {
            TextChannel channel = DiscordBot.client.getTextChannelById(messageId.channelId);
            if (channel == null) continue;
            channel.deleteMessageById(messageId.messageId).queue();
        }
        event.getMessage().delete().queue();
    }
}
