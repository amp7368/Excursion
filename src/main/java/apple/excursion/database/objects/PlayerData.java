package apple.excursion.database.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    private final long id;
    public final String name;
    private final String guildName;
    private final String guildTag;
    public final List<OldSubmission> submissions;
    public final int score;

    public PlayerData(long id, String playerName, String guildName, String guildTag, List<OldSubmission> submissions,int score) {
        this.id = id;
        this.name = playerName;
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissions = submissions;
        if (submissions != null)
            this.submissions.sort((o1, o2) -> (int) (o2.dateSubmitted - o1.dateSubmitted));
        this.score = score;
    }

    public String makeSubmissionHistoryMessage(String name) {
        List<OldSubmission> submissionsThatMatch = new ArrayList<>();
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(name)) {
                submissionsThatMatch.add(submission);
            }
        }
        return String.format(
                "The last time(s) %s %shas done this task:\n%s",
                this.name,
                guildTag == null ? "" : String.format("in [%s] ", guildTag),
                submissionsThatMatch.stream().map(OldSubmission::makeSubmissionHistoryMessage).collect(Collectors.joining("\n")));
    }
}
