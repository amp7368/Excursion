package apple.excursion.discord.reactions.messages.settings;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HelpMessage implements ReactableMessage {
    private final Message message;
    private long lastUpdated = System.currentTimeMillis();
    private int page = 0;
    private static final List<MessageEmbed> helpPages = new ArrayList<>();

    static {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("Introduction");
        embed.setAuthor("Yin", null, DiscordBot.client.getSelfUser().getAvatarUrl());
        embed.setDescription("Hello, Wynncraftian. I am Yin. I am a bot with a list with dozens of locations in-game that you can explore to do fun and unusual activities by yourself, your guild-mates or your friends. These tasks are created by other Wynncraft players as this a minigame created by Wynncraft players, for Wynncraft players. This minigame is called Excursion, while I, Yin, I am a yokai that powers the inanimate object that runs this bot, a Raspberry Pi. I hope we can get along... just in case, though, please read through the other pages to learn how to use me!\n" +
                "\n" +
                "Completing a task earns you Experience Points (EP). EP are points you earn to show everyone else competing that you are the #1 excursionist on my leaderboards. All guilds and players are able to compete in Excursion through my EP leaderboards, but also through some monthly raffles to earn LE. For Raffles, you'll be able to submit tasks and daily tasks to gain EP during the month. There will be prizes for the player with the most EP during the raffle month. You'll be able to find the raffle events on threads via the forums or announced by your guild authorities.");
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("Farplane Discord Info");
        embed.setThumbnail("https://cdn.discordapp.com/icons/555318916344184834/a_afaae5629990a93465456d60339bb1d6.webp?size=128");
        embed.setDescription("Want to create your own tasks, help with the management of submissions," +
                " have a different title other than guest and different colors for your profile, be the first to know of our raffles or " +
                "join a community of dedicated explorers and adventurers from all kinds of guilds and levels? ");
        embed.setImage("https://cdn.discordapp.com/attachments/567540311870668800/767941477527453716/info1.gif");
        embed.addField("Join the Farplane discord!", DiscordBot.EXCURSION_GUILD_INVITE, false);
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("Excursion Benchmarks commands");
        embed.addField("y!lb || y!leaderboard", "This player leaderboard ranks players by their EP of all time, shows their guild, and also shows how much EP a player has earned during the month and the last month", false);
        embed.addField("y!glb || y!gleaderboard", "This guild leaderboard ranks guilds by their EP of all time, shows their player who contributed the most EP, and also shows much much EP a guild has earned during the month and the last month", false);
        embed.addField("y!glb [guild] || y!gleaderboard [guild]", "search for guild profile. it displays the guild progress bar, a ranked list of every player in the guild, and a submission record", false);
        embed.addField("y!profile [player]", "search for player profile. it displays your progress bar for EP, a progress bar for your guild, a personalised recommendation list of tasks by most EP and a submission record", false);
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("History commands");
        embed.addField("y!history || y!ghistory [(optional) guild]", "gives a per month leaderboard for players or guilds or a specific guild. " + AllReactables.Reactable.LEFT.getFirstEmoji() + " and "
                + AllReactables.Reactable.RIGHT.getFirstEmoji() + "  changes pages on this leaderboard " + AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji() + "  and "
                + AllReactables.Reactable.CLOCK_RIGHT.getFirstEmoji() + "  changes the month forward and backwards", false);
        embed.addField("Note", "An optional flag (-m \\|\\| -w \\|\\| -d) with an optional number after can be supplied to give a monthly, weekly, or daily leaderboard.", false);
        embed.addField("Note", "If you supply the optional argument of a guild name or guild tag, then it gives the leaderboard for that guild  and same thing as before", false);
        embed.addField("Example", "__y!ghistory Yin -w 2__ will provide a bi-weekly leaderboard for all players in The Farplane guild", false);
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("Postcard Task commands");
        embed.addField("y!postcard", "List of missions, excursions, dares and total tasks organised in Alphabetical order", false);
        embed.addField("y!postcard [task]", "Search for an specific task and receive its information.", false);
        embed.addField("y!calendar", "Weekly calendar of daily tasks. It displays 3 tasks per week day and 10 tasks for the weekend. I generate a new set of daily tasks at the start of every month", false);
        embed.addField("y!submit [task name] [link] [@mentions]", "Submit tasks when completing them for the first time. This command is also is used for repeating tasks by submitting daily tasks from the calendar to earn soul juice or raise your monthly EP count too to participate in Excursion raffles and win LE for you and your guild. An image can be attached instead of a set of urls. Mentioning players with a tag will include them in your submission.", false);
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("Settings commands");
        embed.addField("y!guild [tag]", "join a guild or change your guild or create a new guild by also providing a guild name", false);
        embed.addField("y!help", "Display this helpful message", false);
        embed.addField("y!bug [message with an optional image]", "Sends a bug report. (Don't hesitate to report any bugs even if it might not be a bug)", false);
        helpPages.add(embed.build());

        embed = new EmbedBuilder();
        embed.setColor(DiscordBot.BOT_COLOR);
        embed.setTitle("CrossChat");
        embed.addField("y!delete [message_id #]", "deletes the message corresponding to that id on all servers **if** it's your own message", false);
        embed.addField("y!discord set", " (used by Administrators of discords) sets the channel to be linked with other discords so you can talk between them (If there is already a linked channel in the discord, this will override that)", false);
        embed.addField("y!discord remove", "(used by Administrators of discords) removes any linked channel in a discord server", false);
        helpPages.add(embed.build());
    }

    public HelpMessage(MessageChannel channel) {
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        return helpPages.get(page);
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        User user = event.getUser();
        if (user == null) {
            return;
        }
        switch (reactable) {
            case LEFT:
                page = Math.max(0, page - 1);
                message.editMessage(makeMessage()).queue();
                lastUpdated = System.currentTimeMillis();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                page = Math.min(helpPages.size() - 1, page + 1);
                message.editMessage(makeMessage()).queue();
                lastUpdated = System.currentTimeMillis();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                page = 0;
                message.editMessage(makeMessage()).queue();
                lastUpdated = System.currentTimeMillis();
                event.getReaction().removeReaction(user).queue();
                break;
        }
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
