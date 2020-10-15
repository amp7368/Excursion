package apple.excursion.discord.reactions.messages;

import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.answers.LeaderboardOfGuilds;
import apple.excursion.discord.data.answers.GuildLeaderboardEntry;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;


public class GuildLeaderboardMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 10;
    private final Message message;
    private int page;
    private long lastUpdated;
    private LeaderboardOfGuilds leaderboard= AllProfiles.getLeaderboardOfGuilds();
    public GuildLeaderboardMessage(MessageChannel channel) {
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

        int entriesLength = leaderboard.leaderboard.size();
        for (int place = page * ENTRIES_PER_PAGE; place < ((page + 1) * ENTRIES_PER_PAGE) && place < entriesLength; place++) {
            StringBuilder stringToAdd = new StringBuilder();
            if (place % 5 == 0) {
                stringToAdd.append(getDash());
            }
            GuildLeaderboardEntry entry = leaderboard.leaderboard.get(place);
            stringToAdd.append(String.format("|%4d|%-20s|%-3s| %9d | %-25s| %-9d|\n",
                    place + 1, entry.guildName, entry.guildTag,entry.points, entry.topPlayer.length()>25?entry.topPlayer.substring(0,22)+"...":entry.topPlayer, entry.topPlayerPoints));

            if (leaderboardMessage.length() + 3 + stringToAdd.length() >= 2000) {
                leaderboardMessage.append("```");
                return leaderboardMessage.toString();
            } else {
                leaderboardMessage.append(stringToAdd);
            }
        }
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("Total EP: %d EP\n",leaderboard.getTotalEp()));
        leaderboardMessage.append(String.format("Total EP guildless players: %d EP\n",leaderboard.getNoGuildsEp()));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append("```");
        return leaderboardMessage.toString();
    }

    private String getDash() {
        return "-".repeat(78) + "\n";
    }

    public void forward() {
        if ((leaderboard.leaderboard.size() - 1) / ENTRIES_PER_PAGE >= page + 1) {
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
