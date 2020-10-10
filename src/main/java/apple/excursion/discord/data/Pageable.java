package apple.excursion.discord.data;

public interface Pageable {
    void forward();
    void backward();
    Long getId();
    long getLastUpdated();
}
