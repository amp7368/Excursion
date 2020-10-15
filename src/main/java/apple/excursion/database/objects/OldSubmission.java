package apple.excursion.database.objects;


import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.utils.Pair;
import apple.excursion.utils.Pretty;

import java.util.List;
import java.util.stream.Collectors;

public class OldSubmission {
    private final SubmissionData.TaskSubmissionType submissionType;
    public int id;
    public Long dateSubmitted;
    public String taskName;
    public String[] links;
    public Pair<String, String> submitter; // id to name
    public List<Pair<String, String>> otherSubmitters; // id to name

    public OldSubmission(int id, Long date, String taskName, String links,
                         Pair<String, String> submitter, List<Pair<String, String>> otherSubmitters,
                         String submissionType) {
        this.id = id;
        this.dateSubmitted = date;
        this.taskName = taskName;
        this.links = links == null ? null : links.split(",");
        this.submitter = submitter;
        this.otherSubmitters = otherSubmitters;
        SubmissionData.TaskSubmissionType submissionTypeTemp;
        try {
            submissionTypeTemp = SubmissionData.TaskSubmissionType.valueOf(submissionType);
        } catch (IllegalArgumentException | NullPointerException e) {
            submissionTypeTemp = SubmissionData.TaskSubmissionType.IDK;
        }
        this.submissionType = submissionTypeTemp;
    }

    public String makeSubmissionHistoryMessage() {
        if (otherSubmitters == null || otherSubmitters.size() == 0) {
            return String.format("**%s** submitted __%s__ at *%s*",
                    submitter.getValue(), taskName, Pretty.date(dateSubmitted));
        }
        return String.format("**%s** submitted __%s__ with **%s** at *%s*",
                submitter.getValue(),
                taskName,
                otherSubmitters.stream().map(Pair::getValue).collect(Collectors.joining(",and ")),
                Pretty.date(dateSubmitted));
    }
}
