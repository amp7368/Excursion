package apple.excursion.discord.reactions;

import apple.excursion.discord.data.leaderboard.LeaderBoard;
import apple.excursion.discord.data.leaderboard.LeaderBoardEntry;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;


public class LeaderBoardMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 20;
    private Message message;
    private int page;
    private long lastUpdated;

    public LeaderBoardMessage(MessageChannel channel) {
        this.page = 0;
        this.message = channel.sendMessage(getMessage()).complete();
        message.addReaction("\u2B05").queue();
        message.addReaction("\u27A1").queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    private String getMessage() {
        StringBuilder leaderboardMessage = new StringBuilder();
        leaderboardMessage.append(String.format("```glsl\nExcursion Leaderboards Page (%d)\n", page + 1));
        leaderboardMessage.append(getDash());
        leaderboardMessage.append(String.format("|%5s", "|"));
        leaderboardMessage.append(String.format(" Name%28s", "|"));
        leaderboardMessage.append(" Total EP |\n");

        int entriesLength = LeaderBoard.leaderBoardEntries.size();
        for (int place = page * ENTRIES_PER_PAGE; place < ((page + 1) * ENTRIES_PER_PAGE) && place < entriesLength; place++) {
            StringBuilder stringToAdd = new StringBuilder();
            if (place % 5 == 0) {
                stringToAdd.append(getDash());
            }
            LeaderBoardEntry entry = LeaderBoard.leaderBoardEntries.get(place);
            stringToAdd.append(String.format("|%4d| %-30s | %8d |\n", place + 1, entry.name, entry.points));

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
        return "-".repeat(50) + "\n";
    }

    public void forward() {
        if (LeaderBoard.leaderBoardEntries.size() / ENTRIES_PER_PAGE >= page + 1) {
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
