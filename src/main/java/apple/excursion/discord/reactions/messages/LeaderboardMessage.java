package apple.excursion.discord.reactions.messages;

import apple.excursion.database.GetDB;
import apple.excursion.database.objects.player.PlayerLeaderboardEntry;
import apple.excursion.discord.data.Profile;
import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;


public class LeaderboardMessage implements ReactableMessage {
    private final PlayerLeaderboard leaderboard = GetDB.getPlayerLeaderboard();
    private static final int ENTRIES_PER_PAGE = 20;
    private final Message message;
    private int page;
    private long lastUpdated;

    public LeaderboardMessage(MessageChannel channel) throws SQLException {
        this.page = 0;
        this.message = channel.sendMessage(getMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        this.lastUpdated = System.currentTimeMillis();
        AllReactables.add(this);
    }

    private String getMessage() {
        StringBuilder leaderboardMessage = new StringBuilder();
        leaderboardMessage.append(String.format("```glsl\nExcursion Leaderboards Page (%d)\n", page + 1));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("|%4s|", ""));
        leaderboardMessage.append(String.format(" %-31s|", "Name"));
        leaderboardMessage.append(String.format(" %8s |", "Total EP"));
        leaderboardMessage.append(String.format(" %-20s|", "Guild Name"));
        leaderboardMessage.append(String.format(" %4s|\n", "Tag "));
        int entriesLength = leaderboard.size();
        for (int place = page * ENTRIES_PER_PAGE; place < ((page + 1) * ENTRIES_PER_PAGE) && place < entriesLength; place++) {
            StringBuilder stringToAdd = new StringBuilder();
            if (place % 5 == 0) {
                stringToAdd.append(getDash());
            }
            PlayerLeaderboardEntry entry = leaderboard.get(place);
            final String name = entry.playerName;
            stringToAdd.append(String.format("|%4d| %-31s| %8d | %-20s| %3s |\n",
                    place + 1, name.length() > 25 ? name.substring(0, 22) + "..." : name, entry.score, entry.guildName, entry.guildTag));

            if (leaderboardMessage.length() + 3 + stringToAdd.length() >= 2000) {
                leaderboardMessage.append("```");
                return leaderboardMessage.toString();
            } else {
                leaderboardMessage.append(stringToAdd);
            }
        }
        leaderboardMessage.append("```");
        return leaderboardMessage.toString();
    }

    private String getDash() {
        return "-".repeat(78) + "\n";
    }

    public void forward() {
        if ((leaderboard.size() - 1) / ENTRIES_PER_PAGE >= page + 1) {
            page++;
            message.editMessage(getMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    public void backward() {
        if (page != 0) {
            page--;
            message.editMessage(getMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    private void top() {
        page = 0;
        message.editMessage(getMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }


    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case LEFT:
                backward();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                forward();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                top();
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
