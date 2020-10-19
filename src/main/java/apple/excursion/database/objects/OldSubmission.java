package apple.excursion.database.objects;


import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pretty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OldSubmission {
    private final SubmissionData.TaskSubmissionType submissionType;
    public int id;
    public long dateSubmitted;
    public String taskName;
    public String[] links;
    public String submitter; // id to name
    public List<String> otherSubmitters; // id to name
    public int score;

    public OldSubmission(ResultSet response) throws SQLException {
        otherSubmitters = new ArrayList<>(Arrays.asList(response.getString(1).split(",")));
        submitter = response.getString(2);
        otherSubmitters.remove(submitter);

        dateSubmitted = response.getTimestamp(3).toInstant().toEpochMilli();
        taskName = response.getString(4);
        String linksRaw = response.getString(5);
        if (linksRaw == null) links = new String[0];
        else links = linksRaw.split(",");
        submissionType = SubmissionData.TaskSubmissionType.valueOf(response.getString(6));
        score = response.getInt(7);
    }

    public String makeSubmissionHistoryMessage() {
        if (otherSubmitters == null || otherSubmitters.size() == 0) {
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
                String.join(", and ", otherSubmitters),
                submissionType == SubmissionData.TaskSubmissionType.SYNC ? "before" : "at",
                Pretty.date(dateSubmitted));
    }
}
