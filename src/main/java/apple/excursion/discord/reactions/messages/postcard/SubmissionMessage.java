package apple.excursion.discord.reactions.messages.postcard;

import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.stream.Collectors;

public class SubmissionMessage implements ReactableMessage {
    private SubmissionData data;
    private User thisReviewer;

    private Message message;

    public SubmissionMessage(SubmissionData data, User reviewer) {
        thisReviewer = reviewer;
        this.data = data;
        message = thisReviewer.openPrivateChannel().complete().sendMessage(makeMessage()).complete();
        message.addReaction("\u2705").queue();
        message.addReaction("\u274C").queue();
        this.data.addMessage(message, thisReviewer);
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("**");
        text.append(data.getSubmitterName());
        text.append("**");
        text.append(" has submitted: ");
        text.append("*");
        text.append(data.getTaskName());
        text.append("*");
        text.append("\n");
        if (data.getSubmittersNameAndIds().isEmpty()) {
            text.append("There are no other submitters.");
        } else {
            text.append("The evidence includes: ");
            text.append(data.getSubmittersNameAndIds().stream().map(Pair::getValue).collect(Collectors.joining(" and ")));
            text.append('.');
        }
        text.append("\n");
        if (!data.getLinks().isEmpty()) {
            text.append("Additional links include:");
            for (String link : data.getLinks()) {
                text.append("\n");
                text.append(link);
            }
            text.append("\n");
        }
        text.append("\n");
        text.append(data.submissionHistoryMessage);
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(data.getColor());
        if (data.getType() == SubmissionData.TaskSubmissionType.DAILY) {
            embed.setTitle(data.getTaskName() + " - Daily Task");
        } else
            embed.setTitle(data.getTaskName());
        embed.setDescription(text);
        if (data.getAttachment() != null)
            embed.setImage(data.getAttachment());

        return embed.build();
    }


    public void acceptSubmit() throws SQLException {
        synchronized (data.sync) {
            if (data.isNotAccepted()) {
                data.setAccepted();
                InsertDB.insertSubmission(data);
            }
        }
    }

    public void completeSubmit(boolean isAccepted, User reviewer) {
        // find all the messages sent to other reviewers
        data.completeSubmit(isAccepted, reviewer);
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        switch (reactable) {
            case ACCEPT:
                try {
                    acceptSubmit();
                } catch (SQLException throwables) {
                    // todo give error message
                    throwables.printStackTrace();
                }
                completeSubmit(true, event.getUser());
                break;
            case REJECT:
                completeSubmit(false, event.getUser());
                break;
        }
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        // never return a value that would lead to deleting the memory of this message
        return System.currentTimeMillis();
    }
}
