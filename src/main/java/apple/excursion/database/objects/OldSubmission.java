package apple.excursion.database.objects;


import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pretty;

import java.sql.ResultSet;
import java.sql.SQLException;
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

    public boolean isSyncSubmission() {
        return submissionType == SubmissionData.TaskSubmissionType.SYNC;
    }

    public OldSubmission(ResultSet response) throws SQLException {
        otherSubmitters = Arrays.asList(response.getString(1).split(","));
        submitter = response.getString(2);
        dateSubmitted = response.getTimestamp(3).toInstant().toEpochMilli();
        taskName = response.getString(4);
        String linksRaw = response.getString(5);
        if (linksRaw == null) links = new String[0];
        else links = linksRaw.split(",");
        submissionType = SubmissionData.TaskSubmissionType.valueOf(response.getString(6));
    }

    public String makeSubmissionHistoryMessage() {
        if (isSyncSubmission()) return "";
        if (otherSubmitters == null || otherSubmitters.size() == 0) {
            return String.format("**%s** submitted %s__%s__ at *%s*",
                    submitter,
                    submissionType == SubmissionData.TaskSubmissionType.DAILY ? "**daily task** " : "",
                    taskName,
                    Pretty.date(dateSubmitted));
        }
        return String.format("**%s** submitted %s__%s__ with **%s** at *%s*",
                submitter,
                submissionType == SubmissionData.TaskSubmissionType.DAILY ? "**daily task** " : "",
                taskName,
                String.join(", and ", otherSubmitters),
                Pretty.date(dateSubmitted));
    }
}
