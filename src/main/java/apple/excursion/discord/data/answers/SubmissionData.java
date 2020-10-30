package apple.excursion.discord.data.answers;

import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.utils.Pair;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static apple.excursion.discord.commands.general.postcard.CommandSubmit.BOT_COLOR;

public class SubmissionData {
    private final int submissionId;
    private boolean isAccepted = false;
    private boolean isCompleted = false;

    private final long epochTimeOfSubmission;

    @Nullable
    private final String attachmentsUrl;
    private final List<String> links;
    private final TaskSimple task;
    private final TaskSubmissionType taskSubmissionType;
    private final String submitter;
    private final long submitterId;
    private final List<Pair<Long, String>> allSubmitters;
    public final String submissionHistoryMessage;
    private final int color;

    public SubmissionData(List<Message.Attachment> attachments, List<String> links,
                          TaskSimple task, String submitter, int color, long submitterId, List<Pair<Long, String>> otherSubmitters,
                          List<PlayerData> playersData, TaskSubmissionType taskType) {
        this.attachmentsUrl = attachments.isEmpty() ? null : attachments.get(0).getUrl();
        this.submissionId = -1;
        this.links = links;
        this.task = task;
        this.color = color;
        this.submitter = submitter;
        this.submitterId = submitterId;
        this.allSubmitters = otherSubmitters;
        this.taskSubmissionType = taskType;
        this.epochTimeOfSubmission = Instant.now().getEpochSecond() * 1000;
        this.submissionHistoryMessage = makeSubmissionHistoryMessage(playersData);
    }

    public SubmissionData(boolean isAccepted, boolean isCompleted, int submissionId, long epochTimeOfSubmission, @Nullable String attachmentsUrl,
                          List<String> links, TaskSimple task, TaskSubmissionType taskSubmissionType,
                          String submitterName, long submitterId, List<Pair<Long, String>> idToNames, List<PlayerData> playersData, int color) {
        this.isAccepted = isAccepted;
        this.isCompleted = isCompleted;
        this.submissionId = submissionId;
        this.epochTimeOfSubmission = epochTimeOfSubmission;
        this.attachmentsUrl = attachmentsUrl;
        this.links = links;
        this.task = task;
        this.taskSubmissionType = taskSubmissionType;
        this.submitter = submitterName;
        this.submitterId = submitterId;
        this.color = color;
        this.allSubmitters = idToNames;
        this.submissionHistoryMessage = makeSubmissionHistoryMessage(playersData);
    }


    private String makeSubmissionHistoryMessage(List<PlayerData> playersData) {
        List<PlayerData> playerDataTemp = new ArrayList<>(playersData);
        playerDataTemp.removeIf(Objects::isNull);
        playerDataTemp.removeIf(playerData -> !playerData.hasSubmissionHistory());
        return playerDataTemp.stream().map(playerData -> playerData.makeSubmissionHistoryMessage(task.name)).collect(Collectors.joining("\n\n"));
    }


    public int completeSubmit(boolean isAccepted, List<Pair<Long, Long>> reviewerMessages, String reviewerName) throws SQLException {
        if (isCompleted) return 0;
        isCompleted = true;
        int submissionId = -1;
        if (isAccepted)
            submissionId = InsertDB.insertSubmission(this);
        for (Pair<Long, Long> channelMessageAndId : reviewerMessages) {
            PrivateChannel channel = DiscordBot.client.getPrivateChannelById(channelMessageAndId.getKey());
            if (channel == null) continue;
            channel.retrieveMessageById(channelMessageAndId.getValue()).queue(other -> {
                if (other == null) return;
                if (isAccepted)
                    other.editMessage("**" + reviewerName + " accepted the submission**\n" + other.getContentStripped()).queue();
                else
                    other.editMessage("**" + reviewerName + " denied the submission**\n" + other.getContentStripped()).queue();

                other.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                other.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
            }, failure -> SendLogs.discordError("Submit", "Failed to notify a reviewer of the accepted/rejected submission"));
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("You have submitted: " + task.name);
        embed.setColor(BOT_COLOR);
        if (attachmentsUrl != null)
            embed.setImage(attachmentsUrl);
        for (Pair<Long, String> userRaw : allSubmitters) {
            DiscordBot.client.retrieveUserById(userRaw.getKey()).queue(user -> {
                if (user != null) {
                    if (user.isBot()) return;
                    user.openPrivateChannel().queue(channel -> {
                        List<String> otherSubmitters = new ArrayList<>();
                        for (Pair<Long, String> otherUserRaw : allSubmitters) {
                            if (!otherUserRaw.getKey().equals(userRaw.getKey())) {
                                otherSubmitters.add(otherUserRaw.getValue());
                            }
                        }
                        StringBuilder text = new StringBuilder();
                        text.append(String.format("**The evidence has been %s**", this.isAccepted ? "accepted!" : "denied."));
                        text.append("\n");
                        if (otherSubmitters.isEmpty()) {
                            text.append("There were no other submitters.");
                        } else {
                            text.append("Players submitted: *");
                            text.append(String.join(", ", otherSubmitters));
                            text.append("*");
                            text.append('.');
                        }
                        if (!links.isEmpty()) {
                            text.append("\nLinks include:");
                            for (String link : links) {
                                text.append("\n");
                                text.append(link);
                            }
                        }
                        embed.setDescription(text);
                        channel.sendMessage(embed.build()).queue();
                    }, failure -> SendLogs.discordError("Submit", String.format("There was an error notifying <%s,%d> of their reviewed submission", userRaw.getValue(), userRaw.getKey())));
                }
            }, failure -> SendLogs.discordError("Submit", String.format("There was an error notifying <%s,%d> of their reviewed submission", userRaw.getValue(), userRaw.getKey())));
        }
        return submissionId;
    }

    public int getColor() {
        return color;
    }

    public void setAccepted() {
        isAccepted = true;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public String getTaskName() {
        return task.name;
    }

    public List<Pair<Long, String>> getSubmittersNameAndIds() {
        return allSubmitters;
    }

    public String getSubmitterName() {
        return submitter;
    }

    public Collection<String> getLinks() {
        return links;
    }

    @Nullable
    public String getAttachment() {
        return attachmentsUrl;
    }

    public long getTimeEpoch() {
        return epochTimeOfSubmission;
    }

    public long getSubmitterId() {
        return submitterId;
    }

    public int getTaskScore() {
        return task.points;
    }

    public TaskSubmissionType getType() {
        return taskSubmissionType;
    }

    public String getCategory() {
        return task.category;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public enum TaskSubmissionType {
        DAILY,
        NORMAL,
        SYNC,
        OLD
    }
}
