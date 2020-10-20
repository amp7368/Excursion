package apple.excursion.discord;

import apple.excursion.ExcursionMain;
import apple.excursion.discord.commands.*;
import apple.excursion.discord.commands.general.settings.CommandSubmit;
import apple.excursion.discord.reactions.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static final String PREFIX = "t!";
    public static final long EXCURSION_GUILD_ID = 555318916344184834L;

    public static String discordToken; // my bot
    public static JDA client;

    public DiscordBot() {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String BOT_TOKEN_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/config/discordToken.data";

        File file = new File(BOT_TOKEN_FILE_PATH);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            discordToken = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in the token for the discord bot in '" + BOT_TOKEN_FILE_PATH + "'");
            System.exit(1);
        }

    }

    public void enableDiscord() throws LoginException {
        JDABuilder builder = new JDABuilder(discordToken);
        builder.addEventListeners(this);
        client = builder.build();
//        client.getPresence().setPresence(Activity.playing(PREFIX + "help"), true);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot() || event.getChannelType() != ChannelType.TEXT) {
            return;
        }
        // the author is not a bot

        String messageContent = event.getMessage().getContentStripped().toLowerCase();
        // deal with the different commands
        for (Commands command : Commands.values()) {
            if (command.isCommand(messageContent)) {
                command.run(event);
                return;
            }
        }
        if (CommandSubmit.isReviewer(event.getAuthor())) {
            for (CommandsAdmin command : CommandsAdmin.values()) {
                if (command.isCommand(messageContent)) {
                    command.run(event);
                    return;
                }
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null || user.isBot()) {
            return;
        }
        AllReactables.dealWithReaction(event);
    }
}
