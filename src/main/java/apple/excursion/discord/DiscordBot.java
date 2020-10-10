package apple.excursion.discord;

import apple.excursion.ExcursionMain;
import apple.excursion.discord.commands.*;
import apple.excursion.discord.reactions.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
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
import java.util.HashMap;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    private static final String PREFIX = "y!";
    public static final String SUBMIT_COMMAND = PREFIX + "submit";
    public static final String LEADERBOARD_COMMAND = PREFIX + "leaderboard";
    public static final String LEADERBOARD_COMMAND2 = PREFIX + "lb";
    private static final String PROFILE_COMMAND = PREFIX + "profile";
    private static final String ADD_REVIEWER_COMMAND = PREFIX + "add_reviewer";
    private static final String REMOVE_REVIEWER_COMMAND = PREFIX + "remove_reviewer";

    private static final HashMap<String, DoCommand> commandMap = new HashMap<>();
    private static final HashMap<String, DoReaction> reactionMap = new HashMap<>();
    public static String discordToken; // my bot
    public static JDA client;

    public DiscordBot() {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String BOT_TOKEN_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/data/discordToken.data";

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
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        commandMap.put(SUBMIT_COMMAND, new CommandSubmit(client));
        commandMap.put(LEADERBOARD_COMMAND, new CommandLeaderBoard());
        commandMap.put(LEADERBOARD_COMMAND2, new CommandLeaderBoard());
        commandMap.put(PROFILE_COMMAND, new CommandProfile());
        commandMap.put(ADD_REVIEWER_COMMAND, new CommandAddReviewer());
        commandMap.put(REMOVE_REVIEWER_COMMAND, new CommandRemoveReviewer());

        reactionMap.put("\u2B05", new ReactionArrowLeft());
        reactionMap.put("\u27A1", new ReactionArrowRight());
        reactionMap.put("\u2705", new ReactionCheckMark());
        reactionMap.put("\u274C", new ReactionExit());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }
        // the author is not a bot

        String messageContent = event.getMessage().getContentStripped();
        // deal with the differenct commands
        for (String command : commandMap.keySet()) {
            if (messageContent.startsWith(command)) {
                commandMap.get(command).dealWithCommand(event);
                break;
            }
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null || user.isBot()) {
            return;
        }
        String emojiName = event.getReactionEmote().getName();
        for (String reaction : reactionMap.keySet()) {
            if (emojiName.equals(reaction)) {
                reactionMap.get(emojiName).dealWithReaction(event);
                break;
            }
        }
    }
}
