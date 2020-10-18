package apple.excursion.discord.reactions.messages;

import apple.excursion.database.GetDB;
import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;


public class GuildLeaderboardMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 10;
    private final Message message;
    private int page;
    private long lastUpdated;
    private final LeaderboardOfGuilds leaderboard = GetDB.getGuildList();

    public GuildLeaderboardMessage(MessageChannel channel) throws SQLException {
        this.page = 0;
        this.message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        this.lastUpdated = System.currentTimeMillis();
        AllReactables.add(this);
    }

    private String makeMessage() {
        StringBuilder leaderboardMessage = new StringBuilder();
        leaderboardMessage.append(String.format("```glsl\nExcursion Guild Leaderboards Page (%d)\n", page + 1));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("|%4s|", ""));
        leaderboardMessage.append(String.format("%-20s|", "Guild Name"));
        leaderboardMessage.append(String.format("%-3s|", "Tag"));
        leaderboardMessage.append(String.format(" %9s |", "Guild EP"));
        leaderboardMessage.append(String.format(" %-25s|", "Top Player"));
        leaderboardMessage.append(String.format(" %-9s|\n", "EP"));

        int entriesLength = leaderboard.size();
        for (int place = page * ENTRIES_PER_PAGE; place < ((page + 1) * ENTRIES_PER_PAGE) && place < entriesLength; place++) {
            StringBuilder stringToAdd = new StringBuilder();
            if (place % 5 == 0) {
                stringToAdd.append(getDash());
            }
            GuildLeaderboardEntry entry = leaderboard.get(place);
            stringToAdd.append(String.format("|%4d|%-20s|%-3s| %9d | %-25s| %-9d|\n",
                    entry.rank,
                    entry.guildName,
                    entry.guildTag,
                    entry.score,
                    entry.topPlayer.length() > 25 ? entry.topPlayer.substring(0, 22) + "..." : entry.topPlayer,
                    entry.topGuildScore));

            if (leaderboardMessage.length() + 3 + stringToAdd.length() >= 2000) {
                leaderboardMessage.append("```");
                return leaderboardMessage.toString();
            } else {
                leaderboardMessage.append(stringToAdd);
            }
        }
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("Total EP: %d EP\n", leaderboard.getTotalEp()));
        leaderboardMessage.append(String.format("Total EP guildless players: %d EP\n", leaderboard.getNoGuildsEp()));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append("```");
        return leaderboardMessage.toString();
    }

    private String getDash() {
        return "-".repeat(78) + "\n";
    }

    public void forward() {
        if ((leaderboard.size() - 1) / ENTRIES_PER_PAGE >= page + 1) {
            page++;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    public void backward() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
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
