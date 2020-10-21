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

import java.util.Arrays;
import java.util.List;

public class HelpMessage implements ReactableMessage {
    private final Message message;
    private long lastUpdated = System.currentTimeMillis();
    private int page = 0;
    private static final List<String> helpPages = Arrays.asList(
            "**y!lb:** player leaderboard. it ranks players by their EP of all time, shows their guild, and also shows how much EP a player has earned during the month and the last month.\n\n" +
                    "**y!glb:** guild leaderboard. it ranks guilds by their EP of all time, shows their player who contributed the most EP, and also shows much much EP a guild has earned during the month and the last month. \n\n" +
                    "**t!history or t!ghistory** - gives a per month leaderboard for players or guilds or a specific guild. " + AllReactables.Reactable.LEFT.getFirstEmoji() + " and "
                    + AllReactables.Reactable.RIGHT.getFirstEmoji() + "  changes pages on this leaderboard " + AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji() + "  and "
                    + AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji() + "  changes the month forward and backwards \n" +
                    "**t!history -m (optional #)** - same thing as t!history, but if you add the # it will be that many months at a time\n" +
                    "**t!history -w (optional #)**  - same thing as t!history, but in weeks. and if the # is provided, then it does that many weeks at a time\n" +
                    "**t!history -d (optional #)**  - same thing as t!history, but in days. and if the # is provided, then it does that many days at a time\n" +
                    "if you supply the option argument of a guild name or guild tag, then it gives the leaderboard for that guild  and same thing as before \n\n" +
                    "**y!glb [guild]:** search for guild profile. it displays the guild progress bar, a ranked list of every player in the guild, and a submission record.\n\n" +
                    "**y!profile** [player]:** search for player profile. it displays your progress bar for EP, a progress bar for your guild, a personalised recommendation list of tasks by most EP and a submission record.",
            "**y!postcard:** list of missions, excursions, dares and total tasks organised in Alphabetical order.\n\n" +
                    "**y!postcard [task]:** search for an specific task and receive its information. it displays the full description, the EP amount and the task creator. \n\n" +
                    "**y!calendar:** weekly calendar of daily tasks. It displays 3 tasks per week day and 10 tasks for the weekend. These tasks are different every day of the year!\n\n" +
                    "**y!submit [task name] [link]:** submit tasks when completing them for the first time. This command is also is used for repeating tasks by submitting daily tasks from the calendar to earn soul juice or raise your monthly EP count too to participate in Excursion raffles and win LE for you and your guild. ",
            "**y!guild [tag]:** join a guild or change your guild.\n\n" +
                    "**y!help:** the command showing you this list"
    );
    private static final List<String> helpTitlePages = Arrays.asList(
            "Excursion Benchmarks commands:", "Postcard Task commands:", "Settings commands:"
    );

    public HelpMessage(MessageChannel channel) {
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(helpTitlePages.get(page));
        embed.setDescription(helpPages.get(page));
        embed.setColor(DiscordBot.BOT_COLOR);
        return embed.build();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        User user = event.getUser();
        if(user == null){
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
