package apple.excursion.discord.commands.general.postcard;

import apple.excursion.ExcursionMain;
import apple.excursion.database.queries.GetCalendarDB;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.messages.postcard.SubmissionMessage;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class CommandSubmit implements DoCommand {
    private static final String REVIEWERS_FILE_PATH = getPath();
    public static final int SUBMISSION_HISTORY_SIZE = 5;

    private static String getPath() {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        return String.join("/", list.subList(0, list.size() - 1)) + "/config/reviewers.json";
    }

    public static final Color BOT_COLOR = new Color(0x4e80f7);
    private static List<User> reviewers;
    private static final Object reviewerSyncObject = new Object();

    public CommandSubmit() {
        synchronized (reviewerSyncObject) {
            try {
                reviewers = loadReviewers(DiscordBot.client);
            } catch (IOException | ParseException e) {
                System.err.println("There was an error with the reviewers json");
                System.exit(1);
            }
        }
    }

    public static boolean isReviewer(User author) {
        synchronized (reviewerSyncObject) {
            return reviewers.contains(author);
        }
    }

    public static List<User> listReviewers() {
        return new ArrayList<>(reviewers);
    }

    public void dealWithCommand(MessageReceivedEvent event) {
        List<Member> tags = event.getMessage().getMentionedMembers();
        Map<Long, String> idsToNamesMap = new HashMap<>();
        String nickName;
        for (Member member : tags) {
            nickName = member.getEffectiveName();
            idsToNamesMap.put(member.getIdLong(), nickName);
        }
        Member author = event.getMember();
        if (author == null) {
            // the author doesn't even exist atm
            return;
        }
        event.getMessage().addReaction(AllReactables.Reactable.WORKING.getFirstEmoji()).queue();
        idsToNamesMap.put(author.getIdLong(), author.getEffectiveName());

        List<Pair<Long, String>> idsToNames = new ArrayList<>();
        for (Map.Entry<Long, String> entry : idsToNamesMap.entrySet()) {
            idsToNames.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        Message eventMessage = event.getMessage();
        String content = eventMessage.getContentStripped();
        List<String> contentList = Arrays.asList(content.split(" "));
        contentList.removeIf(String::isBlank);
        content = String.join(" ", contentList.subList(1, contentList.size()));
        for (Pair<Long, String> mention : idsToNames) {
            content = content.replace("@" + mention.getValue(), "");
        }

        // change all the idToNames to the correct name based on The Farplane discord names
        for (Pair<Long, String> idToName : idsToNames) {
            ColoredName coloredName = GetColoredName.get(idToName.getKey());
            if (coloredName.getName() != null) {
                idToName.setValue(coloredName.getName());
            } else {
                idToName.setValue(ColoredName.getGuestName(idToName.getValue()));
            }
        }
        ColoredName coloredName = GetColoredName.get(author.getIdLong());
        String submitterName = coloredName.getName() == null ? author.getEffectiveName() : coloredName.getName();

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

            final TaskSimple task = SheetsPlayerStats.getTaskSimple(questName);
            if (task == null) {
                event.getChannel().sendMessage(String.format("'%s' is not a valid task name", questName)).queue();
                event.getMessage().removeReaction(AllReactables.Reactable.WORKING.getFirstEmoji(), DiscordBot.client.getSelfUser()).queue();
                return;
            }

            List<PlayerData> playersData = new ArrayList<>();
            for (Pair<Long, String> player : idsToNames) {
                try {
                    playersData.add(GetDB.getPlayerData(player, SUBMISSION_HISTORY_SIZE));
                } catch (SQLException throwables) {
                    event.getChannel().sendMessage("There has been an SQLException getting the submitter's player data").queue();
                    event.getMessage().removeReaction(AllReactables.Reactable.WORKING.getFirstEmoji(), DiscordBot.client.getSelfUser()).queue();
                    return;
                }
            }
            SubmissionData.TaskSubmissionType taskType = getTaskType(task.name);
            synchronized (reviewerSyncObject) {
                SubmissionData submissionData = new SubmissionData(
                        attachment,
                        links,
                        task,
                        submitterName,
                        coloredName.getColor(),
                        author.getIdLong(),
                        idsToNames,
                        playersData,
                        taskType
                );
                try {
                    int responseId = InsertDB.insertIncompleteSubmission(submissionData);

                    for (User reviewer : reviewers) {
                        SubmissionMessage.initialize(submissionData, reviewer.openPrivateChannel().complete(), responseId);
                    }
                } catch (SQLException throwables) {
                    event.getChannel().sendMessage("There has been an SQLException adding submission").queue();
                    return;
                }
            }
            eventMessage.addReaction(AllReactables.Reactable.ACCEPT.getFirstEmoji()).queue();
        }
        event.getMessage().removeReaction(AllReactables.Reactable.WORKING.getFirstEmoji(), DiscordBot.client.getSelfUser()).queue();

    }

    private SubmissionData.TaskSubmissionType getTaskType(String name) {
        List<String> today = GetCalendarDB.getTasksToday(Calendar.getInstance());
        for (String taskName : today)
            if (taskName.equalsIgnoreCase(name)) return SubmissionData.TaskSubmissionType.DAILY;
        return SubmissionData.TaskSubmissionType.NORMAL;
    }

    private static List<User> loadReviewers(JDA client) throws IOException, ParseException {
        List<User> reviewers = new LinkedList<>();
        BufferedReader in = new BufferedReader(new FileReader(new File(REVIEWERS_FILE_PATH)));
        JSONParser parser = new JSONParser();
        Object result = parser.parse(in);
        JSONArray reviewersArray = (JSONArray) result;
        for (Object entry : reviewersArray) {
            reviewers.add(client.retrieveUserById(entry.toString()).complete());
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void removeReviewers(List<User> users) {
        synchronized (reviewerSyncObject) {
            reviewers.removeAll(users);
            try {
                writeReviewers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeReviewers() throws IOException {
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
