package apple.excursion.discord.reactions.messages.benchmark;

import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.database.objects.guild.GuildLeaderboardEntry;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;


public class GuildLeaderboardMessage implements ReactableMessage {
    public static final int ENTRIES_PER_PAGE = 10;
    private final Message message;
    private int page;
    private long lastUpdated;
    private final LeaderboardOfGuilds leaderboard;

    public GuildLeaderboardMessage(MessageChannel channel, LeaderboardOfGuilds leaderboard) {
        this.leaderboard = leaderboard;
        this.page = 0;
        this.message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        this.lastUpdated = System.currentTimeMillis();
        AllReactables.add(this);
    }

    public String makeMessage() {
        String title = String.format("Excursion Guild Leaderboards Page (%d)", page + 1);
        return makeMessageStatic(leaderboard, page, title);
    }

    public static String makeMessageStatic(LeaderboardOfGuilds leaderboard, int page, String title) {
        StringBuilder leaderboardMessage = new StringBuilder();
        leaderboardMessage.append(String.format("```glsl\n%s\n", title));
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
                    entry.getGuildName(),
                    entry.getGuildTag(),
                    entry.score,
                    entry.topPlayer.length() > 25 ? entry.topPlayer.substring(0, 22) + "..." : entry.topPlayer,
                    entry.topPlayerPoints));

            if (leaderboardMessage.length() + 3 + stringToAdd.length() >= 2000) {
                leaderboardMessage.append("```");
                return leaderboardMessage.toString();
            } else {
                leaderboardMessage.append(stringToAdd);
            }
        }
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("Total EP: %d EP\n", leaderboard.getTotalEp()));
        leaderboardMessage.append(String.format("Total EP guildless submissions: %d EP\n", leaderboard.getNoGuildsEp()));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append("```");
        return leaderboardMessage.toString();
    }

    private static String getDash() {
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

    @Override
    public void dealWithOld() {
        message.clearReactions().queue(success -> {
        }, failure -> {
        }); //ignore if we don't have perms. it's really not a bad thing
    }
}
