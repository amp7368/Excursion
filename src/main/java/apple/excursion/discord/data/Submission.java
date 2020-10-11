package apple.excursion.discord.data;

import apple.excursion.discord.DiscordBot;
import apple.excursion.sheets.PlayerStats;
import apple.excursion.sheets.SheetsConstants;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;
import java.util.List;

import static apple.excursion.discord.commands.general.CommandSubmit.BOT_COLOR;

public class Submission {
    private final List<Pair<Long, String>> idToNames;
    public Message message;
    protected List<Message> reviewersMessages;
    private String questName;
    private List<String> links;
    private List<Message.Attachment> attachment;

    public Submission(Message message, List<Pair<Long, String>> idToNames, List<Message> reviewers, String questName,
                      List<String> links, List<Message.Attachment> attachment) {
        this.message = message;
        this.idToNames = idToNames;
        this.reviewersMessages = reviewers;
        this.questName = questName;
        this.links = links;
        this.attachment = attachment;
    }


    public void completeSubmit() {
        for (Pair<Long, String> idToName : idToNames) {
            PlayerStats.submit(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, questName, String.valueOf(idToName.getKey()), idToName.getValue());
        }
    }

    public void confirm(boolean isAccepted) {
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
}
