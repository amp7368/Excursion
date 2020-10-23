package apple.excursion.discord.data.answers;

public class SubmissionWithGuild {
    public String guildName;
    public String guildTag;
    public SubmissionData submissionData;

    public SubmissionWithGuild(String guildName, String guildTag, SubmissionData submissionData) {
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissionData = submissionData;
    }
}
