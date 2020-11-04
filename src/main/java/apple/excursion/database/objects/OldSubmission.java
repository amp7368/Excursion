package apple.excursion.database.objects;


import apple.excursion.database.queries.GetSql;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OldSubmission {
    public final SubmissionData.TaskSubmissionType submissionType;
    private final String image;
    public int id;
    public long dateSubmitted;
    public String taskName;
    public String[] links;
    public String submitter;
    public List<String> otherSubmitters;
    public int score;

    public OldSubmission(ResultSet response) throws SQLException {
        otherSubmitters = new ArrayList<>(Arrays.asList(response.getString(1).split(",")));
        submitter = response.getString(2);
        otherSubmitters.remove(submitter);

        dateSubmitted = response.getTimestamp(3).toInstant().toEpochMilli();
        taskName = GetSql.convertTaskNameFromSql(response.getString(4));
        String linksRaw = response.getString(5);
        if (linksRaw == null) links = new String[0];
        else links = linksRaw.split(",");
        submissionType = SubmissionData.TaskSubmissionType.valueOf(response.getString(6));
        score = response.getInt(7);
        image = response.getString(8);
    }

    public String makeSubmissionHistoryMessage() {
        List<String> otherSubmittersTemp = new ArrayList<>(otherSubmitters);
        otherSubmittersTemp.remove(submitter);
        if (otherSubmitters == null || otherSubmittersTemp.size() == 0) {
            return String.format("**%s** submitted %s__%s__ %s *%s*",
                    submitter,
                    submissionType == SubmissionData.TaskSubmissionType.DAILY ? "**daily task** " : "",
                    taskName,
                    submissionType == SubmissionData.TaskSubmissionType.SYNC ? "before" : "at",
                    Pretty.date(dateSubmitted));
        }
        return String.format("**%s** submitted %s__%s__ with **%s** %s *%s*",
                submitter,
                submissionType == SubmissionData.TaskSubmissionType.DAILY ? "**daily task** " : "",
                taskName,
                String.join(", and ", otherSubmittersTemp),
                submissionType == SubmissionData.TaskSubmissionType.SYNC ? "before" : "at",
                Pretty.date(dateSubmitted));
    }

    public MessageEmbed getDisplay(String author) {
        List<String> otherSubmittersTemp = new ArrayList<>(otherSubmitters);
        otherSubmittersTemp.remove(submitter);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor(author);
        StringBuilder description = new StringBuilder();
        description.append("**");
        description.append(submitter);
        description.append("**");
        description.append(" has submitted: ");
        description.append("*");
        description.append(taskName);
        description.append("*");
        description.append("\n");
        if (otherSubmittersTemp.isEmpty()) {
            description.append("There are no other submitters.");
        } else {
            description.append("The other submitters include: ");
            description.append(String.join(", and", otherSubmittersTemp));
            description.append('.');
        }
        description.append("\n");
        if (links != null && links.length != 0) {
            description.append("Additional links include:");
            for (String link : links) {
                description.append("\n");
                description.append(link);
            }
            description.append("\n");
        }
        description.append("\n");
        embed.setDescription(description);
        if (image != null)
            embed.setImage(image);

        return embed.build();
    }
}
