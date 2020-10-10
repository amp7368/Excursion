package apple.excursion.discord.data.leaderboard;

import apple.excursion.discord.data.Pageable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;


public class LeaderBoardMessage implements Pageable {
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
        leaderboardMessage.append(String.format("|%5s","|"));
        leaderboardMessage.append(String.format(" Name%28s","|"));
        leaderboardMessage.append(" Total EP |\n");

        int entriesLength = LeaderBoard.leaderBoardEntries.size();
        for (int place = page * ENTRIES_PER_PAGE; place < ((page + 1) * ENTRIES_PER_PAGE) && place < entriesLength; place++) {
            StringBuilder stringToAdd = new StringBuilder();
            if (place % 5 == 0) {
                stringToAdd.append(getDash());
            }
            LeaderBoardEntry entry = LeaderBoard.leaderBoardEntries.getOrDefault(place + 1, null);
            if (entry == null) {
                stringToAdd.append(String.format("Could not get place for #%d", place + 1));
            } else
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
        return "--------------------------------------------------\n";
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

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
