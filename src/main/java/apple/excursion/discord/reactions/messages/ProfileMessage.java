package apple.excursion.discord.reactions.messages;

import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.Profile;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.answers.GuildLeaderboardProfile;
import apple.excursion.discord.data.answers.PlayerLeaderboardProfile;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.PostcardDisplay;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileMessage implements ReactableMessage {
    private static final int NUM_OF_CHARS_PROGRESS = 20;
    private static final Color BOT_COLOR = new Color(0x4e80f7);
    private Message message;
    private Profile profile;
    private GuildLeaderboardProfile guildProfile;
    private PlayerLeaderboardProfile playerLeaderboardProfile;
    private final Map<String, List<TaskSimple>> topTasks = new HashMap<>();
    private long lastUpdated = System.currentTimeMillis();
    private final List<Task> tasks = SheetsTasks.getTasks();

    public ProfileMessage(MessageReceivedEvent event) {
        final String[] eventContentSplitOnce = event.getMessage().getContentStripped().split(" ", 2);
        if (eventContentSplitOnce.length > 1) {
            final String nameToGet = eventContentSplitOnce[1];
            List<Profile> profilesWithName = AllProfiles.getProfile(nameToGet);
            final int profilesWithNameLength = profilesWithName.size();
            if (profilesWithNameLength == 0) {
                // quit with an error message
                event.getChannel().sendMessage(String.format("Nobody's name contains '%s'.", nameToGet)).queue();
                return;
            } else if (profilesWithNameLength == 1) {
                // we found the person
                profile = profilesWithName.get(0);
            } else {
                // ask the user to narrow their search
                //todo what if multiple ppl have the same name
                event.getChannel().sendMessage(String.format("There are %d people that have '%s' in their name.", profilesWithNameLength, nameToGet)).queue();
                return;
            }
        } else {
            profile = AllProfiles.getProfile(event.getAuthor().getIdLong(), event.getMember().getEffectiveName());
            if (profile == null) {
                event.getChannel().sendMessage("There was an error making a new profile for you").queue();
                return;
            }
        }
        guildProfile = AllProfiles.getLeaderboardOfGuilds().getGuildProfile(profile.getGuild());
        playerLeaderboardProfile = AllProfiles.getOverallLeaderboard().getPlayerProfile(profile.getId());
        for (String taskType : TaskSimple.TaskCategory.values())
            topTasks.put(taskType, playerLeaderboardProfile.getTopTasks(taskType));
        message = event.getChannel().sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        int i = 0;
        for (List<TaskSimple> tasks : topTasks.values()) {
            final int size = tasks.size();
            for (int j = 0; j < size; j++) {
                message.addReaction(AllReactables.emojiAlphabet.get(i++)).queue();
            }
        }
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(profile.getName());
        StringBuilder description = new StringBuilder();

        // put guild info
        if (guildProfile == null) {
            description.append(String.format("Not in a guild. To join a guild use %s", "no implemented"));
        } else {
            description.append(String.format("Member of %s [%s]\n", profile.getGuild(), profile.getGuildTag()));
            description.append('\n');
            description.append(String.format("Guild rank: #%d\n", guildProfile.getRank()));
            description.append(getProgressBar(guildProfile.getProgress()));
            description.append('\n');
            description.append(String.format("Guild EP: %d EP", guildProfile.getTotalEp()));
            description.append('\n');
        }
        description.append('\n');

        // put player info
        if (playerLeaderboardProfile == null) {
            // todo message if player is null
        } else {
            description.append(String.format("Player rank #%d\n", playerLeaderboardProfile.getRank()));
            description.append(getProgressBar(playerLeaderboardProfile.getProgress()));
            description.append('\n');
            description.append(String.format("%d out of %d tasks done\n", playerLeaderboardProfile.getCountTasksDone(), playerLeaderboardProfile.getCountTasksTotal()));
            description.append(String.format("Total EP: %d EP\n", playerLeaderboardProfile.getTotalEp()));
            description.append('\n');
            int i = 0;
            for (String taskType : TaskSimple.TaskCategory.values()) {
                description.append(String.format("**Uncompleted %s**\n", taskType));
                List<String> taskNames = new ArrayList<>();
                for (TaskSimple task : topTasks.get(taskType)) {
                    taskNames.add(AllReactables.emojiAlphabet.get(i++) + task.name);
                }
                description.append(String.join("\n", taskNames));
                description.append('\n');
                description.append('\n');
            }
        }
        description.append("**Submission record:** (not implemented)\n");

        embed.setDescription(description);
        embed.setColor(BOT_COLOR);
        return embed.build();
    }

    private String getProgressBar(double percentage) {
        StringBuilder result = new StringBuilder();
        int length = (int) (percentage * NUM_OF_CHARS_PROGRESS);
        result.append("\u2588".repeat(Math.max(0, length)));
        length = NUM_OF_CHARS_PROGRESS - length;
        result.append("\u2591".repeat(Math.max(0, length)));
        return result.toString();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null) return;
        if (reactable == AllReactables.Reactable.ALPHABET) {
            final int size = AllReactables.emojiAlphabet.size();
            for (int i = 0; i < size; i++) {
                if (AllReactables.emojiAlphabet.get(i).equals(event.getReactionEmote().getName())) {
                    // we found the emote
                    int j = 0;
                    for (List<TaskSimple> tasks : topTasks.values()) {
                        if (tasks.size() + j > i) {
                            TaskSimple taskFound = tasks.get(i - j);
                            Task task = getTaskFromSimple(taskFound);
                            if (task != null) {
                                message.editMessage(PostcardDisplay.getMessage(task)).queue();
                            }
                            break;
                        }
                        j += tasks.size();
                    }
                    event.getReaction().removeReaction(user).queue();
                }
            }
            lastUpdated = System.currentTimeMillis();
        } else if (reactable == AllReactables.Reactable.TOP) {
            message.editMessage(makeMessage()).queue();
            event.getReaction().removeReaction(user).queue();
        }
    }

    @Nullable
    private Task getTaskFromSimple(TaskSimple taskFound) {
        for (Task task : tasks) {
            if (task.taskName.equalsIgnoreCase(taskFound.name) && task.category.equalsIgnoreCase(taskFound.category)) {
                return task;
            }
        }
        return null;
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
