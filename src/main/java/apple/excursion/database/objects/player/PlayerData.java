package apple.excursion.database.objects.player;

import apple.excursion.database.VerifyDB;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.discord.commands.general.postcard.CommandSubmit;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.answers.SubmissionData;

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

    public PlayerData(long id, String playerName, String guildName, String guildTag, List<OldSubmission> submissions, int score, int soulJuice) {
        this.id = id;
        this.name = playerName;
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissions = submissions;
        this.soulJuice = soulJuice;
        this.score = score;
    }

    public String makeSubmissionHistoryMessage(String name) {
        List<OldSubmission> submissionsThatMatch = new ArrayList<>();
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(name)) {
                submissionsThatMatch.add(submission);
            }
        }
        submissionsThatMatch.sort((s1, s2) -> {
            long c = (s2.dateSubmitted - s1.dateSubmitted);
            return c > 0 ? 1 : c == 0 ? 0 : -1;
        });
        if (submissionsThatMatch.size() > CommandSubmit.SUBMISSION_HISTORY_SIZE)
            submissionsThatMatch = submissionsThatMatch.subList(0, CommandSubmit.SUBMISSION_HISTORY_SIZE);
        return submissionsThatMatch.isEmpty() ? "" :
                String.format(
                        "The last time(s) %s %shas done this task:\n%s",
                        this.name,
                        guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG) ? "" : String.format("in [%s] ", guildTag),
                        submissionsThatMatch.stream().map(OldSubmission::makeSubmissionHistoryMessage).collect(Collectors.joining("\n")));
    }

    public boolean containsSubmission(Task task) {
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(task.name)) return true;
        }
        return false;
    }

    public int getSoulJuice() {
        return soulJuice;
    }

    public int getScoreOfSubmissionsWithName(String taskName) {
        int score = 0;
        for (OldSubmission submission : submissions) {
            if (submission.taskName.equalsIgnoreCase(taskName)) {
                score += submission.score;
            }
        }
        return score;
    }

    public boolean hasSubmissionHistory() {
        return !submissions.isEmpty();
    }
}
