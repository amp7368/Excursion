package apple.excursion.discord.commands.general.settings;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.commands.general.postcard.CommandSubmit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandBugReport implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> content = new ArrayList<>(Arrays.asList(event.getMessage().getContentStripped().split(" ")));
        content.remove(0);
        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        if (attachments.isEmpty() && content.isEmpty()) {
            event.getChannel().sendMessage("Explain the bug or attach an image in your message").queue();
            return;
        }
        EmbedBuilder bugReport = new EmbedBuilder();
        bugReport.setTitle("Bug Report");
        if (!attachments.isEmpty()) {
            bugReport.setImage(attachments.get(0).getUrl());
        }
        bugReport.setDescription(String.join(" ", content) + "\n\nThis bug report is from " + event.getAuthor().getName() + " with id of " + event.getAuthor().getId());
        MessageEmbed bugReportMessage = bugReport.build();
        for (User reviewer : CommandSubmit.listReviewers()) {
            reviewer.openPrivateChannel().complete().sendMessage(bugReportMessage).queue();
        }
        event.getChannel().sendMessage("Thank you for reporting this.").queue();
    }
}
