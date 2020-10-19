package apple.excursion.discord.reactions.messages.history;

import apple.excursion.database.queries.GetDB;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.discord.data.answers.HistoryGuildLeaderboard;
import apple.excursion.discord.data.answers.HistoryLeaderboardOfGuilds;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.discord.reactions.messages.leaderboard.GuildProfileMessage;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static apple.excursion.discord.reactions.messages.leaderboard.CalendarMessage.EPOCH_START_OF_EXCURSION;
import static apple.excursion.discord.reactions.messages.leaderboard.CalendarMessage.EPOCH_START_OF_SUBMISSION_HISTORY;

public class SpecificGuildHistoryMessage implements ReactableMessage {
    private final Message message;
    private final Map<Long, HistoryGuildLeaderboard> leaderboard = new HashMap<>();
    private final Calendar timeLookingAt = Calendar.getInstance();
    private final Calendar later;
    private final int timeInterval;
    private final int timeField;
    private final String guildName;
    private final String guildTag;
    private long lastUpdated = System.currentTimeMillis();
    private int page = 0;

    public SpecificGuildHistoryMessage(MessageChannel channel, int timeInterval, int timeField, String guildTag, String guildName) {
        later = Calendar.getInstance();
        later.setTimeInMillis(timeLookingAt.getTimeInMillis());
        later.add(timeField, timeInterval);

        this.timeInterval = timeInterval;
        this.timeField = timeField;
        this.guildTag = guildTag;
        this.guildName = guildName;
        timeLookingAt.add(timeField, -timeInterval + 1); // make it the last 3 days instead of the next 3
        this.message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_RIGHT.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private MessageEmbed makeMessage() {
        if (later.getTimeInMillis()<=timeLookingAt.getTimeInMillis()) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Future");
            embed.setDescription("This data for this time is not available yet");
            return embed.build();
        }
        HistoryGuildLeaderboard myLeaderboard = leaderboard.get(timeLookingAt.getTimeInMillis());
        if (myLeaderboard == null) {
            try {
                HistoryLeaderboardOfGuilds leaderboardOfGuilds = GetDB.getGuildLeaderboard(timeField, timeInterval, timeLookingAt);
                List<PlayerData> players = GetDB.getPlayersInGuild(guildTag, leaderboardOfGuilds.startTime, leaderboardOfGuilds.endTime);
                List<OldSubmission> submissions = GetDB.getGuildSubmissions(guildTag, leaderboardOfGuilds.startTime, leaderboardOfGuilds.endTime);
                GuildLeaderboardEntry matchedGuild = leaderboardOfGuilds.leaderboard.get(guildTag, guildName);
                if (matchedGuild == null)
                    matchedGuild = new GuildLeaderboardEntry(guildTag, guildName, 0, "nobody", 0);
                myLeaderboard = new HistoryGuildLeaderboard(
                        matchedGuild,
                        players,
                        submissions,
                        leaderboardOfGuilds.startTime,
                        leaderboardOfGuilds.endTime);
                leaderboard.put(timeLookingAt.getTimeInMillis(), myLeaderboard);

            } catch (SQLException throwables) {
                throwables.printStackTrace();//todo deal with error
            }
        }
        long startTime = myLeaderboard.startTime;
        long endTime = myLeaderboard.endTime;
        if (startTime < EPOCH_START_OF_SUBMISSION_HISTORY) {
            startTime = EPOCH_START_OF_EXCURSION;
        }
        String title = String.format("%s [%s]", guildName, guildTag);
        String headerTitle = String.format("[%s to %s]\n",
                Pretty.date(startTime),
                Pretty.date(endTime));
        return GuildProfileMessage.makeMessageStatic(myLeaderboard.matchedGuild, myLeaderboard.players, myLeaderboard.submissions, page, title, headerTitle);
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case LEFT:
                left();
                message.removeReaction(reaction, user).queue();
                break;
            case RIGHT:
                right();
                message.removeReaction(reaction, user).queue();
                break;
            case TOP:
                top();
                message.removeReaction(reaction, user).queue();
                break;
            case CLOCK_LEFT:
                timeLeft();
                message.removeReaction(reaction, user).queue();
                break;
            case CLOCK_RIGHT:
                timeRight();
                message.removeReaction(reaction, user).queue();
                break;
        }
    }

    private void timeRight() {
        timeLookingAt.add(timeField, timeInterval);
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    private void timeLeft() {
        if (timeLookingAt.getTimeInMillis() < EPOCH_START_OF_SUBMISSION_HISTORY)
            return;
        timeLookingAt.add(timeField, -timeInterval);
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    private void right() {
        HistoryGuildLeaderboard myLeaderboard = leaderboard.get(timeLookingAt.getTimeInMillis());
        if ((myLeaderboard.players.size() - 1) / GuildProfileMessage.ENTRIES_PER_PAGE >= page + 1) {
            page++;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    private void left() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
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
