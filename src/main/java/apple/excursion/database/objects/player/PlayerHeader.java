package apple.excursion.database.objects.player;

public class PlayerHeader {
    public long id;
    public String name;
    public int score;
    public int soulJuice;

    public PlayerHeader(long id, String name, int soulJuice, int score) {
        this.id = id;
        this.name = name;
        this.soulJuice = soulJuice;
        this.score = score;
    }
}
