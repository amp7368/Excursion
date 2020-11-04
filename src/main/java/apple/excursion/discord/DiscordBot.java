package apple.excursion.discord;

import apple.excursion.BackupThread;
import apple.excursion.ExcursionMain;
import apple.excursion.discord.commands.*;
import apple.excursion.discord.commands.general.postcard.CommandSubmit;
import apple.excursion.discord.cross_chat.CrossChat;
import apple.excursion.discord.data.DailyBans;
import apple.excursion.discord.listener.AllChannelListeners;
import apple.excursion.discord.reactions.*;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.MigrateOldSubmissions;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiscordBot extends ListenerAdapter {
    public static final String PREFIX = "y!";
    public static final long EXCURSION_GUILD_ID = 555318916344184834L;
    public static final long APPLEPTR16 = 253646208084475904L;
    public static final int BOT_COLOR = 0x4e80f7;
    public static final String EXCURSION_GUILD_INVITE = "https://discord.gg/6je2sZj";

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
        DailyBans.isBan(""); // just make sure the static is done in that class
        JDABuilder builder = JDABuilder.create(discordToken, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.DIRECT_MESSAGE_REACTIONS);
        builder.addEventListeners(this);
        client = builder.build();
        client.getPresence().setPresence(Activity.playing("y!help"), true);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        new SheetsTasks().start();
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //        new MigrateOldSubmissions().start();
        new BackupThread().start();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }
        AllChannelListeners.dealWithMessage(event);
        if (event.getChannelType() != ChannelType.TEXT) {
            return;
        }
        // the author is not a bot

        String messageContent = event.getMessage().getContentStripped().toLowerCase();
        try {
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
            Member member = event.getMember();
            if (member != null && (member.hasPermission(Permission.ADMINISTRATOR) || member.isOwner())) {
                for (CommandsManageServer command : CommandsManageServer.values()) {
                    if (command.isCommand(messageContent)) {
                        command.run(event);
                        return;
                    }
                }
            }

            // this isn't a command
            CrossChat.dealWithMessage(event);
        } catch (InsufficientPermissionException e) {
            SendLogs.sendLogs(Collections.singletonList(
                    String.format(e.getGuild(client).getName() + " did not give me the perms: " + e.getPermission().getName())
            ));
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null || user.isBot()) {
            return;
        }
        try {
            AllReactables.dealWithReaction(event);
            if (event.isFromGuild())
                CrossChat.dealWithReaction(event);
            DatabaseResponseReactable.dealWithReaction(event);
        } catch (InsufficientPermissionException e) {
            SendLogs.sendLogs(Collections.singletonList(
                    String.format(e.getGuild(client).getName() + " did not give me the perms: " + e.getPermission().getName())
            ));
        }
    }
}
