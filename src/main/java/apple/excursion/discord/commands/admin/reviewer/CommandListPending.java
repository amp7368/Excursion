package apple.excursion.discord.commands.admin.reviewer;

import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.List;

public class CommandListPending implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        User author = event.getAuthor();
        author.openPrivateChannel().queue(channel -> {
            List<Pair<Long, Long>> responses;
            try {
                responses = GetDB.getResponseMessages(channel.getIdLong());
            } catch (SQLException throwables) {
                channel.sendMessage("There was an SQLException getting a list of pending submissions").queue();
                return;
            }
            StringBuilder messages = new StringBuilder();
            messages.append("Below is all the pending submissions\n");
            for (Pair<Long, Long> response : responses) {
                String append = String.format("https://discord.com/channels/@me/%d/%d\n", response.getKey(), response.getValue());
                if (messages.length() + append.length() > 2000) {
                    channel.sendMessage(messages.toString()).queue();
                    messages = new StringBuilder();
                } else {
                    messages.append(append);
                }
            }
            if (messages.length() != 0) {
                channel.sendMessage(messages.toString()).queue();
            }
        });
    }
}
