package apple.excursion.discord.data.leaderboard;

import java.util.List;

public class LeaderBoardEntry {
    public long discordId;
    public String name;
    public int points = 0;
    public String guildName;
    public String guildTag;

    public LeaderBoardEntry(List<Object> entry, int endIndex) {
        Object discordIdObject = entry.get(0);
        if (discordIdObject instanceof String) {
            try {
                discordId = Long.parseLong((String) discordIdObject);
            } catch (NumberFormatException e) {
                discordId = -1;
            }
        } else if (discordIdObject instanceof Long) {
            discordId = (long) discordIdObject;
        } else
            discordId = -1;

        name = entry.get(1).toString();
        guildName = entry.get(2).toString();
        guildTag = entry.get(3).toString();

        for (Object task : entry.subList(4, endIndex)) {
            if (task != null)
                if (task instanceof String) {
                    try {
                        points += Long.parseLong((String) task);
                    } catch (NumberFormatException ignored) {
                    }
                } else if (task instanceof Integer)
                    points += (int) task;
        }
    }
}
