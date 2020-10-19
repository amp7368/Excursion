package apple.excursion.discord.reactions.messages.history;

import apple.excursion.database.GetDB;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.discord.data.answers.HistoryLeaderboardOfGuilds;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.discord.reactions.messages.GuildLeaderboardMessage;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class GuildHistoryMessage implements ReactableMessage {
    private final Message message;
    private Map<Long, LeaderboardOfGuilds> leaderboard = new HashMap<>();
    private Calendar timeLookingAt = Calendar.getInstance();
    private int timeInterval;
    private int timeField;
    private long lastUpdated = System.currentTimeMillis();
    private int page = 0;
    private long startTime;
    private long endTime;

    public GuildHistoryMessage(MessageChannel channel, int timeInterval, int timeField) {
        this.timeInterval = timeInterval;
        this.timeField = timeField;
        timeLookingAt.add(timeField, -timeInterval + 1); // make it the last 3 days instead of the next 3
        this.message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_RIGHT.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private String makeMessage() {
        LeaderboardOfGuilds myLeaderboard = leaderboard.get(timeLookingAt.getTimeInMillis());
        if (myLeaderboard == null) {
            try {
                HistoryLeaderboardOfGuilds answer = GetDB.getGuildLeaderboard(timeField, timeInterval, timeLookingAt);
                myLeaderboard = answer.leaderboard;
                leaderboard.put(timeLookingAt.getTimeInMillis(), myLeaderboard);
                startTime = answer.startTime;
                endTime = answer.endTime;
            } catch (SQLException throwables) {
                throwables.printStackTrace();//todo deal with error
            }
        }
        String title = String.format("Excursion Guild Leaderboards Page (%d)\n[%s to %s]",
                page + 1,
                Pretty.date(startTime),
                Pretty.date(endTime));
        return GuildLeaderboardMessage.makeMessageStatic(myLeaderboard, page, title);
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
    }

    private void timeLeft() {
        timeLookingAt.add(timeField, -timeInterval);
        message.editMessage(makeMessage()).queue();
    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
    }

    private void right() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    private void left() {
        LeaderboardOfGuilds myLeaderboard = leaderboard.get(timeLookingAt.getTimeInMillis());
        if ((myLeaderboard.size() - 1) / GuildLeaderboardMessage.ENTRIES_PER_PAGE >= page + 1) {
            page++;
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
