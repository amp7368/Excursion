package apple.excursion.database;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    private final long id;
    private final String name;
    private final String guildName;
    private final String guildTag;
    public final List<OldSubmission> submissions;

    public PlayerData(long id, String playerName, String guildName, String guildTag, List<OldSubmission> submissions) {
        this.id = id;
        this.name = playerName;
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissions = submissions;
        this.submissions.sort((o1, o2) -> (int) (o2.dateSubmitted-o1.dateSubmitted));
    }

    public String makeSubmissionHistoryMessage(String name) {
        List<OldSubmission> submissionsThatMatch = new ArrayList<>();
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(name)) {
                submissionsThatMatch.add(submission);
            }
        }
        return String.format(
                "The last time(s) %s in [%s] has done this task:\n%s",
                this.name,
                guildTag,
                submissionsThatMatch.stream().map(OldSubmission::makeSubmissionHistoryMessage).collect(Collectors.joining("\n")));
    }
}
