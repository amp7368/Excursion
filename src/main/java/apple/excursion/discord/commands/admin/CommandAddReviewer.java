package apple.excursion.discord.commands.admin;

import apple.excursion.discord.commands.general.settings.CommandSubmit;
import apple.excursion.discord.commands.DoCommand;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.LinkedList;
import java.util.List;

public class CommandAddReviewer implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        if (CommandSubmit.isReviewer(event.getAuthor())) {
            List<Member> tags = event.getMessage().getMentionedMembers();
            List<User> users = new LinkedList<>();
            List<String> names = new LinkedList<>();
            for (Member tag : tags) {
                User user = tag.getUser();
                users.add(user);
                names.add(user.getName());
            }
            CommandSubmit.addReviewers(users);
            event.getChannel().sendMessage(String.format("I added %s to the list of reviewers.", String.join(", ", names))).queue();
        }
    }
}
