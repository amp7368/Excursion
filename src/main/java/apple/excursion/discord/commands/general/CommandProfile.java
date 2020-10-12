package apple.excursion.discord.commands.general;

import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.Profile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class CommandProfile implements DoCommand {
    private static final int NUM_OF_CHARS_PROGRESS = 20;
    private static final Color BOT_COLOR = new Color(0x4e80f7);

    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        Profile profile;
        final String[] eventContentSplit = event.getMessage().getContentStripped().split(" ", 2);
        if (eventContentSplit.length > 1) {
            final String nameToGet = eventContentSplit[1];
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
        }
//        StringBuilder text = new StringBuilder();
//        int questsNotDoneSize = profile.tasksNotDone.size();
//        double percentage = profile.tasksDone.size() / (double) (profile.tasksDone.size() + questsNotDoneSize);
//        text.append(getProgressBar(percentage));
//        text.append(" ");
//        text.append((int) (percentage * 100));
//        text.append("% of completed tasks");
//        text.append(String.format("\n\n__*Total EP: %s*__\n", NumberFormat.getIntegerInstance().format(profile.totalEp)));
//        text.append('\n');
//        text.append("__*Uncompleted tasks:*__\n");
//        int sizeToDisplay = Math.min(20, questsNotDoneSize);
//        text.append(String.join(" **\u2022** ", profile.tasksNotDone.subList(0, sizeToDisplay)));
//        if (sizeToDisplay < questsNotDoneSize) {
//            text.append(String.format("...and **%d** more.", questsNotDoneSize - sizeToDisplay));
//        }
//        text.append('\n');
//        EmbedBuilder embed = new EmbedBuilder();
//        if (profileMember != null) {
//            String nickname = profileMember.getEffectiveName();
//            embed.setTitle(nickname);
//        }
//        embed.setDescription(text.substring(0, Math.min(text.length(), 1999)));
//        embed.setColor(BOT_COLOR);
//
//        event.getChannel().sendMessage(embed.build()).queue();
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
