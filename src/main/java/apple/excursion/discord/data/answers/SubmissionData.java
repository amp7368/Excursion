package apple.excursion.discord.data.answers;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static apple.excursion.discord.commands.general.CommandSubmit.BOT_COLOR;

public class SubmissionData {
    public final Object sync = new Object();
    private boolean isAccepted = false;
    private boolean isCompleted = false;

    private String acceptor;
    private final Map<Long, Long> reviewerIdToMessageId = new HashMap<>();
    private final List<Message> reviewerMessages = new ArrayList<>();
    private final long epochTimeOfSubmission = Instant.now().getEpochSecond();

    @Nullable
    private final String attachmentsUrl;
    private final List<String> links;
    private final String taskName;
    private final String submitter;
    private final List<Pair<Long, String>> allSubmitters;

    public SubmissionData(List<Message.Attachment> attachments, List<String> links,
                          String taskName, String submitter, List<Pair<Long, String>> otherSubmitters) {
        this.attachmentsUrl = attachments.isEmpty() ? null : attachments.get(0).getUrl();
        this.links = links;
        this.taskName = taskName;
        this.submitter = submitter;
        this.allSubmitters = otherSubmitters;
    }

    public void addMessage(Message message, User reviewer) {
        synchronized (sync) {
            this.reviewerIdToMessageId.put(reviewer.getIdLong(), message.getIdLong());
            this.reviewerMessages.add(message);
        }
    }

    public void setAccepted() {
        isAccepted = true;
    }

    public boolean isNotAccepted() {
        return !isAccepted;
    }

    public String getTaskName() {
        return taskName;
    }

    public List<Pair<Long, String>> getSubmittersIds() {
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

    public void completeSubmit(boolean isAccepted, User reviewer) {
        synchronized (sync) {
            if (isCompleted) return;
            isCompleted = true;
            for (Message other : reviewerMessages) {
                AllReactables.remove(other.getIdLong());
                if (isAccepted)
                    other.editMessage("**" + reviewer.getName() + " accepted the submission**\n" + other.getContentStripped()).queue();
                else
                    other.editMessage("**" + reviewer.getName() + " denied the submission**\n" + other.getContentStripped()).queue();

                other.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                other.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
            }

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("You have submitted: " + taskName);
            embed.setColor(BOT_COLOR);
            if (attachmentsUrl != null)
                embed.setImage(attachmentsUrl);
            for (Pair<Long, String> userRaw : allSubmitters) {
                User user = DiscordBot.client.getUserById(userRaw.getKey());
                if (user != null) {
                    PrivateChannel channel = user.openPrivateChannel().complete();
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
                }
            }
        }
    }
}
