package apple.excursion.discord.data.leaderboard;

import java.util.List;

public class LeaderBoardEntry {
    public String name;
    public int points;

    public LeaderBoardEntry(List<Object> entry) {
        if (entry.size() != 2) {
            // idk what happened but whatever..
            name = "null";
            points = -1;
        } else {
            Object nameObject = entry.get(0);
            if (nameObject instanceof String) {
                name = (String) nameObject;
            } else
                name = "null";

            Object pointsObject = entry.get(1);
            if (pointsObject instanceof String) {
                try {
                    points = Integer.parseInt((String) pointsObject);
                } catch (NumberFormatException e) {
                    points = -1;
                }
            } else if (pointsObject instanceof Integer) {
                points = (int) pointsObject;
            } else
                points = -1;
        }
    }
}
