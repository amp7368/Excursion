package apple.excursion.database.objects.player;

import apple.excursion.database.VerifyDB;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerLeaderboardEntry {
    public String playerName;
    public String guildTag;
    public String guildName;
    public int score;
    public int rank;
    private long id;
    public long everyonesScore;

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

    public boolean nameIsSimilar(String name) {
        return playerName.toLowerCase().contains(name);
    }

    public boolean guildIsDefault() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG);
    }

    public long getId() {
        return id;
    }

    public double getProgress() {
        return score / everyonesScore;
    }
}
