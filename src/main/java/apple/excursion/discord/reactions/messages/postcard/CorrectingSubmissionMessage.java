package apple.excursion.discord.reactions.messages.postcard;

import apple.excursion.database.queries.UpdateDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import apple.excursion.utils.Pretty;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.List;

public class CorrectingSubmissionMessage implements ReactableMessage {
    private static final int CORRECTION_COLOR = 0xe33636;
    private final int responseId;
    private final SubmissionData submissionData;
    private final boolean isAccepting;
    private final Message message;
    private final long lastUpdated = System.currentTimeMillis();
    private final List<Pair<Long, Long>> reviewerMessages;

    public CorrectingSubmissionMessage(int responseId, SubmissionData submissionData, boolean isAccepting, List<Pair<Long, Long>> reviewerMessages, MessageChannel channel) {
        this.responseId = responseId;
        this.submissionData = submissionData;
        this.isAccepting = isAccepting;
        this.reviewerMessages = reviewerMessages;
        this.message = channel.sendMessage(makeMessage("")).complete();
        AllReactables.add(this);
        message.addReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
    }

    private MessageEmbed makeMessage(String extra) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Reviewing Correction" + extra);
        embed.setColor(CORRECTION_COLOR);
        embed.setTitle(String.format("From %s to %s",
                submissionData.isAccepted() ? "ACCEPTED" : "REJECTED",
                isAccepting ? "ACCEPTED" : "REJECTED"));
        embed.setDescription(SubmissionMessage.makeMessage(submissionData).getDescription());
        embed.addBlankField(false);
        embed.addBlankField(false);
        embed.addField("Are you sure you want to change the verdict on this submission?", "", false);
        embed.setFooter("React " + AllReactables.Reactable.ACCEPT + " to confirm the change (change the verdict).\n" +
                "React " + AllReactables.Reactable.REJECT + " to reject the change (keep the previous verdict).");
        return embed.build();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        switch (reactable) {
            case ACCEPT:
                confirm(event.getUser());
                AllReactables.remove(getId());
                message.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                message.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
                break;
            case REJECT:
                message.editMessage(makeMessage(" ABANDONED")).queue();
                AllReactables.remove(getId());
                message.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
                message.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
                break;
        }
    }

    private void confirm(User user) {
        message.editMessage(makeMessage(" CONFIRMED")).queue();
        try {
            if (isAccepting) {
                // this is not implemented and probably won't be.
            } else {
                // remove the submission
                UpdateDB.removeSubmission(submissionData.getSubmissionId());
                // update the response status so if we change it again, it's still good
                UpdateDB.updateResponseStatus(isAccepting, true, responseId, submissionData.getSubmissionId());

                String reviewerName = GetColoredName.get(user.getIdLong()).getName();
                if (reviewerName == null) reviewerName = user.getName();
                String finalReviewerName = reviewerName;

                for (Pair<Long, Long> reviewerMsgId : reviewerMessages) {
                    PrivateChannel channel = DiscordBot.client.getPrivateChannelById(reviewerMsgId.getKey());
                    if (channel != null) {
                        channel.retrieveMessageById(reviewerMsgId.getValue()).queue(
                                msg -> {
                                    msg.editMessage(finalReviewerName + " corrected the submission and denied it.\n" + msg.getContentRaw()).queue();
                                }, failure -> {
                                }
                        );
                    }
                }
                for (Pair<Long, String> submitter : submissionData.getSubmittersNameAndIds()) {
                    DiscordBot.client.retrieveUserById(submitter.getKey()).queue(submitterUser -> {
                        submitterUser.openPrivateChannel().queue(
                            channel->{
                                channel.sendMessage(String.format("```\nAfter closer examination, your submission %s at %s was denied. Sorry about that correction.\n```", submissionData.getTaskName(), Pretty.date(submissionData.getTimeEpoch()))).queue();
                            }, failure -> SendLogs.discordError("CorrectSubmit", String.format("There was an error notifying <%s,%d> of their reviewed submission", submitter.getValue(), submitter.getKey()))
                        );
                    }, failure -> SendLogs.discordError("CorrectSubmit", String.format("There was an error notifying <%s,%d> of their reviewed submission", submitter.getValue(), submitter.getKey())));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public void dealWithOld() {
        message.removeReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue(success -> {
        }, failure -> {
        }); //ignore fails
        message.removeReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue(success -> {
        }, failure -> {
        }); //ignore fails
    }
}
