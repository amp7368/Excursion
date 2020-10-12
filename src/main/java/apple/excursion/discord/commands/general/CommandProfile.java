package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.Profile;
import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.answers.GuildLeaderboardProfile;
import apple.excursion.discord.data.answers.PlayerLeaderboardProfile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class CommandProfile implements DoCommand {
    private static final int NUM_OF_CHARS_PROGRESS = 20;
    private static final Color BOT_COLOR = new Color(0x4e80f7);

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        AllProfiles.update();
        Profile profile;
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
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(profile.getName());
        StringBuilder description = new StringBuilder();

        // put guild info
        final GuildLeaderboardProfile guildProfile = AllProfiles.getLeaderboardOfGuilds().getGuildProfile(profile.getGuild());
        if (guildProfile == null) {
            // todo message if guild is null
        } else {
            description.append(String.format("Member of %s [%s]\n", profile.getGuild(), profile.getGuildTag()));
            description.append('\n');
            description.append(String.format("Guild rank: #%d\n", guildProfile.getRank()));
            description.append(getProgressBar(guildProfile.getProgress()));
            description.append('\n');
        }
        description.append('\n');

        // put player info
        final PlayerLeaderboardProfile playerLeaderboardProfile = AllProfiles.getOverallLeaderboard().getPlayerProfile(profile.getId());
        if (playerLeaderboardProfile == null) {
            // todo message if player is null
        } else {
            description.append(String.format("Player rank #%d\n", playerLeaderboardProfile.getRank()));
            description.append(getProgressBar(playerLeaderboardProfile.getProgress()));
            description.append('\n');
            description.append(String.format("Tasks done: %d out of %d tasks\n", playerLeaderboardProfile.getCountTasksDone(), playerLeaderboardProfile.getCountTasksTotal()));
            description.append(String.format("Total EP: %d EP\n", playerLeaderboardProfile.getTotalEp()));
            description.append('\n');
            for (String taskType : TaskSimple.TaskCategory.values()) {
                description.append(String.format("**Uncompleted %s**\n", taskType));
                description.append(playerLeaderboardProfile.getTopTasks(taskType).stream().map(task -> task.name).collect(Collectors.joining(	" **\u2022** ")));
                description.append('\n');
                description.append('\n');
            }
        }
        description.append("**Submission record:** (not implemented)\n");

        embed.setDescription(description);
        embed.setColor(BOT_COLOR);

        event.getChannel().sendMessage(embed.build()).queue();
    }

    private String getProgressBar(double percentage) {
        StringBuilder result = new StringBuilder();
        int length = (int) (percentage * NUM_OF_CHARS_PROGRESS);
        for (int i = 0; i < length; i++)
            result.append('\u2588');
        length = NUM_OF_CHARS_PROGRESS - length;
        for (int i = 0; i < length; i++)
            result.append('\u2591');
        return result.toString();
    }
}
