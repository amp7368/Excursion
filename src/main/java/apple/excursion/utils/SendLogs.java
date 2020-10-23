package apple.excursion.utils;

import apple.excursion.discord.DiscordBot;
import net.dv8tion.jda.api.entities.PrivateChannel;

import java.util.List;

public class SendLogs {
    public static void sendLogs(List<String> logs) {
        PrivateChannel dms = DiscordBot.client.getUserById(DiscordBot.APPLEPTR16).openPrivateChannel().complete();
        StringBuilder builder = new StringBuilder();
        for (String log : logs) {
            if (log.length() + builder.length() > 1999) {
                dms.sendMessage(builder.toString()).queue();
                builder = new StringBuilder();
            }
            builder.append(log);
            builder.append('\n');
        }
        if (builder.length() != 0)
            dms.sendMessage(builder.toString()).queue();
    }
}
