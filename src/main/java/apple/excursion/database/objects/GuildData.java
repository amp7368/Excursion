package apple.excursion.database.objects;

import java.util.ArrayList;
import java.util.List;

public class GuildData {
    public String tag;
    public String name;
    public int score;
    public String topPlayer;
    public int playerScore;
    public final List<OldSubmission> submissions;

    public GuildData(String guildTag, String guildName, int guildScore, String playerName, int playerScore) {
        this.tag = guildTag;
        this.name = guildName;
        this.score = guildScore;
        this.topPlayer = playerName;
        this.playerScore = playerScore;
        submissions = new ArrayList<>();
    }

    public GuildData(String tag, String name, int score, String topPlayer, int playerScore, List<OldSubmission> submissions) {
        this.tag = tag;
        this.name = name;
        this.score = score;
        this.topPlayer = topPlayer;
        this.playerScore = playerScore;
        this.submissions = submissions;
    }
}
