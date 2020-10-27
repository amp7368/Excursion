package apple.excursion.utils;

import apple.excursion.database.queries.SyncDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.answers.SubmissionData;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.sheets.profiles.AllProfiles;
import apple.excursion.sheets.profiles.Profile;
import net.dv8tion.jda.api.entities.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;


public class MigrateOldSubmissions {
    private static final String titleRegex = "You have submitted: .*";
    private static final String everythingRegex = "(.*\\n*)*";
    private static final String acceptedDescriptionRegex = "\\*\\*The evidence has been accepted!\\*\\*" + everythingRegex;

    public static void migrate() throws IOException, SQLException {
        List<Task> allTasks = SheetsTasks.getTasks();
        List<SubmissionData> submissions = new ArrayList<>();
        Set<Profile> profiles = AllProfiles.getProfiles();
        for (Profile profile : profiles) {
            long id = profile.getId();
            if (id == DiscordBot.APPLEPTR16) continue; // skip me because i have a lot of fake submissions
            System.out.println("Player " + profile.getName() + " has been migrated");
            User user = DiscordBot.client.retrieveUserById(id).complete();
            if (user == null || user.isBot()) continue;
            PrivateChannel dms = user.openPrivateChannel().complete();
            if (dms == null) continue;
            MessageHistory history = dms.getHistoryFromBeginning(100).complete();
            List<Message> messages = history.getRetrievedHistory();
            while (!messages.isEmpty()) {
                for (Message message : messages) {
                    List<MessageEmbed> embeds = message.getEmbeds();
                    for (MessageEmbed embed : embeds) {
                        String title = embed.getTitle();
                        String description = embed.getDescription();
                        if (title != null && title.matches(titleRegex) &&
                                description != null && description.matches(acceptedDescriptionRegex)) {
                            title = title.replaceAll("You have submitted: ", "").trim();
                            Task task = null;
                            for (Task taskInAll : allTasks) {
                                Pattern patternForward = Pattern.compile(".*" + taskInAll.name + ".*", Pattern.CASE_INSENSITIVE);
                                Pattern patternBackward = Pattern.compile(".*" + title + ".*", Pattern.CASE_INSENSITIVE);
                                if (patternBackward.matcher(taskInAll.name).matches() || patternForward.matcher(title).matches()) {
                                    task = taskInAll;
                                    break;
                                }
                            }
                            if (task == null) continue;

                            List<String> links = new ArrayList<>();
                            for (String word : description.split(" ")) {
                                if (word.startsWith("http")) {
                                    links.add(word);
                                }
                            }
                            String playerName = profile.getName();
                            long playerId = profile.getId();
                            List<Message.Attachment> attachments = message.getAttachments();
                            String attachment = attachments.isEmpty() ? null : attachments.get(0).getUrl();
                            SubmissionData submissionData = new SubmissionData(
                                    true,
                                    true,
                                    message.getTimeCreated().toEpochSecond() * 1000,
                                    attachment,
                                    links,
                                    new TaskSimple(task.points, task.name, task.category),
                                    SubmissionData.TaskSubmissionType.OLD,
                                    playerName,
                                    playerId,
                                    Collections.singletonList(new Pair<>(playerId, playerName)),
                                    Collections.emptyList(),
                                    ColoredName.getGuestColor()
                            );
                            submissions.add(submissionData);
                        }
                    }
                }
                messages = history.retrieveFuture(100).complete();
            }
        }
        List<String> logs = SyncDB.sync(submissions, profiles, allTasks);
        SendLogs.sendLogs(logs);
        System.out.println("Done migrating old submissions");
    }
}
