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
    private final long id;
    public long topPlayerScore;

    public PlayerLeaderboardEntry(ResultSet response) throws SQLException {
        playerName = response.getString(1);
        guildTag = response.getString(2);
        guildName = response.getString(3);
        score = response.getInt(4);
        id = response.getLong(5);
    }

    public PlayerLeaderboardEntry(String playerName, String guildTag, String guildName, int score, long id) {
        this.playerName = playerName;
        this.guildTag = guildTag;
        this.guildName = guildName;
        this.score = score;
        this.id = id;
    }

    public boolean hasId(long id) {
        return this.id == id;
    }

    public boolean nameIsSimilar(String name) {
        return playerName.toLowerCase().contains(name.toLowerCase());
    }

    public boolean guildIsDefault() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG);
    }

    public long getId() {
        return id;
    }

    public double getProgress() {
        return ((double) score) / topPlayerScore;
    }

    public String getGuildName() {
        return guildName.equals(VerifyDB.DEFAULT_GUILD_NAME) ? "" : guildName;
    }

    public String getGuildTag() {
        return guildTag.equals(VerifyDB.DEFAULT_GUILD_TAG) ? "" : guildTag;
    }
}
