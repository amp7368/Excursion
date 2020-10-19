package apple.excursion.database.objects.player;

import apple.excursion.database.objects.OldSubmission;
import apple.excursion.discord.data.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    public final String name;
    private final String guildName;
    private final String guildTag;
    public final List<OldSubmission> submissions;
    public final int score;
    private final int soulJuice;
    public final long id;

    public PlayerData(long id,String playerName, String guildName, String guildTag, List<OldSubmission> submissions, int score, int soulJuice) {
        this.id = id;
        this.name = playerName;
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissions = submissions;
        this.soulJuice = soulJuice;
        if (submissions != null)
            this.submissions.sort((o1, o2) -> (int) (o2.dateSubmitted / 1000L - o1.dateSubmitted / 1000L));
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

    public boolean containsSubmission(Task task) {
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(task.taskName)) return true;
        }
        return false;
    }

    public int getSoulJuice() {
        return soulJuice;
    }

    public int getScoreOfSubmissionsWithName(String taskName) {
        int score = 0;
        for(OldSubmission submission:submissions){
            if(submission.taskName.equalsIgnoreCase(taskName)){
                score += submission.score;
            }
        }
        return score;
    }
}
