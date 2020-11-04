package apple.excursion.discord.reactions;

import apple.excursion.database.queries.GetDB;
import apple.excursion.database.queries.UpdateDB;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.listener.ResponseListener;
import apple.excursion.discord.reactions.messages.postcard.CorrectingSubmissionMessage;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class DatabaseResponseReactable {
    public static synchronized void dealWithReaction(@NotNull MessageReactionAddEvent event) {
        if (event.getUser() == null) return;
        if (event.getChannelType() == ChannelType.PRIVATE) {
            try {
                Pair<Integer, SubmissionData> responseIdAndSubmissionData = GetDB.getSubmissionData(event.getChannel().getIdLong(), event.getMessageIdLong());
                if (responseIdAndSubmissionData == null) return; // this is not a valid message to respond to
                SubmissionData submissionData = responseIdAndSubmissionData.getValue();
                Integer responseId = responseIdAndSubmissionData.getKey();

                List<Pair<Long, Long>> reviewerMessages = GetDB.getResponseMessages(responseId);

                String reviewerName = GetColoredName.get(event.getUserIdLong()).getName();
                if (reviewerName == null) reviewerName = event.getUser().getName();

                String emoji = event.getReactionEmote().getEmoji();
                if (emoji.equals(AllReactables.Reactable.ACCEPT.getFirstEmoji())) {
                    // accept the response
                    if (submissionData.isCompleted()) {
                        event.getChannel().sendMessage("This submission has already been " + (submissionData.isAccepted() ? "accepted." : "denied")).queue();
                        return;
                    } else {
                        submissionData.setAccepted();
                        int submissionId = submissionData.completeSubmit(true, reviewerMessages, reviewerName);
                        UpdateDB.updateResponseStatus(true, true, responseId, submissionId);
                    }
                } else if (emoji.equals(AllReactables.Reactable.REJECT.getFirstEmoji())) {
                    if (submissionData.isCompleted()) {
                        event.getChannel().sendMessage("This submission has already been " + (submissionData.isAccepted() ? "accepted." : "denied")).queue();
                        if (submissionData.isAccepted()) {
                            // if the reviewer tries to react to change a submission and the reaction would change something
                            new CorrectingSubmissionMessage(responseId, submissionData, false, reviewerMessages, event.getChannel());
                        }
                        return;
                    } else {
                        submissionData.completeSubmit(false, reviewerMessages, reviewerName);
                        UpdateDB.updateResponseStatus(false, true, responseId, -1);
                    }
                } else if (emoji.equals(AllReactables.Reactable.RESPOND.getFirstEmoji())) {
                    new ResponseListener(event.getChannel(), submissionData.getSubmittersNameAndIds());
                }
            } catch (SQLException throwables) {
                event.getChannel().sendMessage("There was an SQLException doing stuff").queue();
                throwables.printStackTrace();
            } catch (IllegalArgumentException throwables) {
                event.getChannel().sendMessage("There is no task name with that name. tell appleptr16").queue();
                throwables.printStackTrace();
            }
        }
    }
}
