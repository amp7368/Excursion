package apple.excursion.utils;

import apple.excursion.ExcursionMain;
import apple.excursion.database.VerifyDB;
import apple.excursion.discord.DiscordBot;
import com.google.common.net.MediaType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SendLogs {
    private static boolean IS_CHANNEL;
    private static long SENDER;

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String path = String.join("/", list.subList(0, list.size() - 1)) + "/config/logSender.data";
        File file = new File(path);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in who to send logs to in '" + path + "'");
            System.exit(1);
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            SENDER = Long.parseLong(reader.readLine());
            IS_CHANNEL = Boolean.parseBoolean(reader.readLine());
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in who to send logs to in '" + path + "'");
            System.exit(1);
        }
    }

    public static void sendLogs(List<String> logs) {
        MessageChannel dms;
        if (IS_CHANNEL) {
            dms = DiscordBot.client.getTextChannelById(SENDER);
        } else {
            dms = DiscordBot.client.retrieveUserById(SENDER).complete().openPrivateChannel().complete();//todo
        }
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

    public static void sendDbBackup() {
        MessageChannel dms;
        if (IS_CHANNEL) {
            dms = DiscordBot.client.getTextChannelById(SENDER);
        } else {
            dms = DiscordBot.client.retrieveUserById(SENDER).complete().openPrivateChannel().complete();//todo
        }
        dms.sendMessage("Below is the DB as of " + Pretty.date(System.currentTimeMillis())).queue();
        synchronized (VerifyDB.syncDB) {
            List<File> files = VerifyDB.getFiles();
            for (File file : files) {
                dms.sendFile(file).complete();
            }
        }
    }

    public static void error(String module, String message) {
        MessageChannel dms;
        if (IS_CHANNEL) {
            dms = DiscordBot.client.getTextChannelById(SENDER);
        } else {
            dms = DiscordBot.client.retrieveUserById(SENDER).complete().openPrivateChannel().complete();//todo
        }
        dms.sendMessage(String.format("[%s] %s", module, message)).queue();

    }

    public static void discordError(String module, String message) {
        MessageChannel dms;
        if (IS_CHANNEL) {
            dms = DiscordBot.client.getTextChannelById(SENDER);
        } else {
            dms = DiscordBot.client.retrieveUserById(SENDER).complete().openPrivateChannel().complete();//todo
        }
        dms.sendMessage(String.format("[%s] %s", module, message)).queue();
    }
}
