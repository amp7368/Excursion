package apple.excursion.discord.reactions.messages.postcard;

import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.sql.SQLException;
import java.util.stream.Collectors;

public class SubmissionMessage {
    public static void initialize(SubmissionData data, PrivateChannel reviewer, int responseId) throws SQLException {
        Message message = reviewer.sendMessage(makeMessage(data)).complete();
        message.addReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.REJECT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RESPOND.getFirstEmoji()).queue();
        InsertDB.insertIncompleteSubmissionLink(message.getIdLong(), reviewer.getIdLong(), responseId);
    }

    public static MessageEmbed makeMessage(SubmissionData data) {
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
        try {
            User user = DiscordBot.client.retrieveUserById(data.getSubmitterId()).complete();
            embed.setThumbnail(user.getAvatarUrl());
        } catch (ErrorResponseException ignored) { //it's really not important to get the user's avatar
        }
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
}
