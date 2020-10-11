package apple.excursion.discord.commands.general;

import apple.excursion.ExcursionMain;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.SubmissionMessage;
import apple.excursion.sheets.PlayerStats;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;

public class CommandSubmit implements DoCommand {
    private static final String REVIEWERS_FILE_PATH = getPath();

    private static String getPath() {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        return String.join("/", list.subList(0, list.size() - 1)) + "/data/reviewers.json";
    }

    public static final Color BOT_COLOR = new Color(0x4e80f7);
    private static List<User> reviewers;
    private static final Object reviewerSyncObject = new Object();

    public CommandSubmit() {
        synchronized (reviewerSyncObject) {
            try {
                reviewers = loadReviewers(DiscordBot.client);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isReviewer(User author) {
        synchronized (reviewerSyncObject) {
            return reviewers.contains(author);
        }
    }

    public void dealWithCommand(MessageReceivedEvent event) {
        List<Member> tags = event.getMessage().getMentionedMembers();
        List<Pair<Long, String>> idToName = new ArrayList<>();
        List<String> otherSubmitters = new ArrayList<>();
        String nickName;
        for (Member member : tags) {
            nickName = member.getNickname();
            if (nickName == null)
                nickName = member.getEffectiveName();
            idToName.add(new Pair<>(member.getIdLong(), nickName));
            otherSubmitters.add(nickName);
        }
        Member author = event.getMember();
        if (author == null) {
            // the author doesn't even exist atm
            return;
        }
        nickName = author.getEffectiveName();
        idToName.add(new Pair<>(author.getIdLong(), nickName));
        String submitter = nickName;

        Message eventMessage = event.getMessage();
        String content = eventMessage.getContentStripped();
        List<String> contentList = Arrays.asList(content.split(" "));
        content = String.join(" ", contentList.subList(1, contentList.size()));
        for (Pair<Long, String> pair : idToName) {
            String other = pair.getValue();
            content = content.replace("@" + other, "");
        }
        content = content.replace("@" + submitter, "").trim();

        String[] contentArray = content.split(" ");
        StringBuilder questNameBuilder = new StringBuilder();
        List<String> links = new ArrayList<>();
        for (String word : contentArray) {
            if (word.startsWith("http")) {
                links.add(word);
            } else {
                questNameBuilder.append(word);
                questNameBuilder.append(' ');
            }
        }
        String questName = questNameBuilder.toString().trim();

        List<Message.Attachment> attachment = event.getMessage().getAttachments();
        if (attachment.size() == 0 && links.isEmpty()) {
            event.getChannel().sendMessage("Attach evidence to submit a task!").queue();
        } else {
            // we have attachments

            if (!PlayerStats.isQuest(questName)) {
                event.getChannel().sendMessage(String.format("'%s' is not a valid task name", questName)).queue();
                return;
            }
            // verify discord nickname
            PlayerStats.verifyDiscordNickname(submitter, event.getAuthor().getIdLong());
            for (Member tag : tags) {
                nickName = tag.getNickname();
                if (nickName == null)
                    nickName = tag.getEffectiveName();
                PlayerStats.verifyDiscordNickname(nickName, tag.getIdLong());
            }
            List<Pair<Long, String>> idToNameTemp = idToName;
            idToName = new ArrayList<>();
            for (Pair<Long, String> submit : idToNameTemp) {
                idToName.add(new Pair<>(submit.getKey(), "__" + submit.getValue() + "__"));
            }

            StringBuilder text = new StringBuilder();
            text.append("**");
            text.append(submitter);
            text.append("**");
            text.append(" has submmited: ");
            text.append("*");
            text.append(questName);
            text.append("*");
            text.append("\n");
            if (otherSubmitters.isEmpty()) {
                text.append("There are no other submitters.");
            } else {
                text.append("The evidence includes: ");
                text.append(String.join(" and ", otherSubmitters));
                text.append('.');
            }
            if (!links.isEmpty()) {
                text.append("\nAdditional links include:");
                for (String link : links) {
                    text.append("\n");
                    text.append(link);
                }
            }
            List<Message> messages = new ArrayList<>();
            synchronized (reviewerSyncObject) {
                for (User reviewer : reviewers) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setColor(BOT_COLOR);
                    embed.setTitle(questName);
                    embed.setDescription(text);
                    for (Message.Attachment file : attachment) {
                        embed.setImage(file.getUrl());
                        break;
                    }
                    MessageAction messageToSend = reviewer.openPrivateChannel().complete().sendMessage(embed.build());

                    Message message = messageToSend.complete();
                    message.addReaction("\u2705").queue();
                    message.addReaction("\u274C").queue();
                    messages.add(message);
                }
                for (Message message : messages) {
                    new SubmissionMessage(message, idToName, messages, questName, links, attachment);
                }
            }
            eventMessage.addReaction("\u2705").queue();
        }
    }

    private static List<User> loadReviewers(JDA client) throws IOException, ParseException {
        List<User> reviewers = new LinkedList<>();
        BufferedReader in = new BufferedReader(new FileReader(new File(REVIEWERS_FILE_PATH)));
        JSONParser parser = new JSONParser();
        Object result = parser.parse(in);
        JSONArray reviewersArray = (JSONArray) result;
        for (Object entry : reviewersArray) {
            reviewers.add(client.getUserById(entry.toString()));
        }
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reviewers;
    }

    public static void addReviewers(List<User> users) {
        synchronized (reviewerSyncObject) {
            for (User user : users)
                if (!reviewers.contains(user))
                    reviewers.add(user);
            try {
                writeReviewers();
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeReviewers(List<User> users) {
        synchronized (reviewerSyncObject) {
            reviewers.removeAll(users);
            try {
                writeReviewers();
            } catch (URISyntaxException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeReviewers() throws URISyntaxException, IOException {
        JSONArray reviewersArray = new JSONArray();
        for (User user : reviewers) reviewersArray.add(user.getId());
        File file = new File(REVIEWERS_FILE_PATH);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create new file: " + REVIEWERS_FILE_PATH);
            }
        }
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
        out.write(reviewersArray.toJSONString());
        out.flush();
        out.close();
    }


}
