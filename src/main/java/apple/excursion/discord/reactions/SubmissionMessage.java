package apple.excursion.discord.reactions;

import apple.excursion.discord.DiscordBot;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.sheets.SheetsConstants;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.LinkedList;
import java.util.List;

import static apple.excursion.discord.commands.general.CommandSubmit.BOT_COLOR;

public class SubmissionMessage implements ReactableMessage {
    private final List<Pair<Long, String>> idToNames;
    private final long id;
    public Message message;
    private List<Message> reviewersMessages;
    private String questName;
    private List<String> links;
    private List<Message.Attachment> attachment;

    public SubmissionMessage(Message message, List<Pair<Long, String>> idToNames, List<Message> reviewers, String questName,
                             List<String> links, List<Message.Attachment> attachment) {
        this.message = message;
        this.id = message.getIdLong();
        this.idToNames = idToNames;
        this.reviewersMessages = reviewers;
        this.questName = questName;
        this.links = links;
        this.attachment = attachment;
        AllReactables.add(this);
    }


    public void completeSubmit() {
        for (Pair<Long, String> idToName : idToNames) {
            SheetsPlayerStats.submit(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, questName, String.valueOf(idToName.getKey()), idToName.getValue());
        }
    }

    public void confirm(boolean isAccepted, User reviewer) {
        // find all the messages sent to other reviewers
        for (Message other : reviewersMessages) {
            if (isAccepted)
                other.editMessage("**" + reviewer.getName() + " accepted the submission**\n" + other.getContentStripped()).queue();
            else
                other.editMessage("**" + reviewer.getName() + " denied the submission**\n" + other.getContentStripped()).queue();

            other.removeReaction("\u274C").queue();
            other.removeReaction("\u2705").queue();
            AllReactables.remove(other.getIdLong());
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("You have submitted: " + questName);
        embed.setColor(BOT_COLOR);
        for (Message.Attachment file : attachment) {
            embed.setImage(file.getUrl());
            break;
        }
        for (Pair<Long, String> userRaw : idToNames) {
            User user = DiscordBot.client.getUserById(userRaw.getKey());
            if (user != null) {
                PrivateChannel channel = user.openPrivateChannel().complete();
                List<String> otherSubmitters = new LinkedList<>();
                for (Pair<Long, String> otherUserRaw : idToNames) {
                    if (!otherUserRaw.getKey().equals(userRaw.getKey())) {
                        otherSubmitters.add(otherUserRaw.getValue());
                    }
                }
                StringBuilder text = new StringBuilder();
                text.append(String.format("**The evidence has been %s**", isAccepted ? "accepted!" : "denied."));
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

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        switch (reactable) {
            case ACCEPT:
                completeSubmit();
                confirm(true, event.getUser());
                break;
            case REJECT:
                confirm(false, event.getUser());
                break;
        }
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public long getLastUpdated() {
        // never return a value that would lead to deleting the memory of this message
        return System.currentTimeMillis();
    }
}
