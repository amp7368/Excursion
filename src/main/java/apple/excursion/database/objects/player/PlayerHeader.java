package apple.excursion.database.objects.player;

public class PlayerHeader {
    public final String guildName;
    public final String guildTag;
    public final long id;
    public final  String name;
    public  final int score;
    public final  int soulJuice;

    public PlayerHeader(long id, String name, int soulJuice, int score,String guildName,String guildTag) {
        this.id = id;
        this.name = name;
        this.soulJuice = soulJuice;
        this.score = score;
        this.guildName = guildName;
        this.guildTag = guildTag;
    }
}
