package apple.excursion.discord.commands.admin;

import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.commands.CommandsAdmin;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandFakeSubmit implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        List<String> contentSplit = new ArrayList<>(Arrays.asList(event.getMessage().getContentStripped().split(" ")));
        contentSplit.remove(0);
        if (contentSplit.size() < 4) {
            event.getChannel().sendMessage(CommandsAdmin.FAKE_SUBMIT.getUsageMessage()).queue();
            return;
        }
        long submitterId;
        int points;
        try {
            submitterId = Long.parseLong(contentSplit.remove(0));
            points = Integer.parseInt(contentSplit.remove(0));
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage(CommandsAdmin.FAKE_SUBMIT.getUsageMessage()).queue();
            return;
        }
        String submitterName = contentSplit.remove(0);
        ColoredName coloredName = GetColoredName.get(submitterId);
        submitterName = coloredName.getName() == null ? submitterName : coloredName.getName();

        String taskCategory = contentSplit.remove(0);
        String taskName = String.join(" ", contentSplit);
        try {
            InsertDB.insertSubmission(new SubmissionData(
                    Collections.emptyList(), // no attachments
                    Collections.emptyList(), // no links
                    new TaskSimple(points, taskName, taskCategory),
                    "", // submitter name isn't important
                    0, // color isn't important
                    submitterId,
                    Collections.singletonList(new Pair<>(submitterId, submitterName)), // no other submitters
                    Collections.emptyList(), // submissionHistory is not needed
                    SubmissionData.TaskSubmissionType.SYNC // this is to sync stuff.
            ));
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There has been an SQLException trying to insert this submission.").queue();
            return;
        }
        event.getChannel().sendMessage(String.format("Successfully submitted %s [%s] for %d EP for player <%d,%s>", taskName, taskCategory, points, submitterId, submitterName)).queue();
    }
}
