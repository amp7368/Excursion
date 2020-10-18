package apple.excursion.database.objects.player;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerLeaderboardEntry {
    public String playerName;
    public String guildTag;
    public String guildName;
    public int score;
    public int rank;
    private long id;


    public PlayerLeaderboardEntry(ResultSet response) throws SQLException {
        playerName = response.getString(1);
        guildTag = response.getString(2);
        guildName = response.getString(3);
        score = response.getInt(4);
        id = response.getLong(5);
    }

    public boolean hasId(long id) {
        return this.id == id;
    }
}
