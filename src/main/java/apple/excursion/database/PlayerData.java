package apple.excursion.database;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    private final long id;
    private final String name;
    private final String guildName;
    private final String guildTag;
    private final List<OldSubmission> submissions;

    public PlayerData(long id, String playerName, String guildName, String guildTag, List<OldSubmission> submissions) {
        this.id = id;
        this.name = playerName;
        this.guildName = guildName;
        this.guildTag = guildTag;
        this.submissions = submissions;
    }

    @Override
    public String toString() {
        return String.format("id: %d\nname: %s\nguildName: %s\nguildTag: %s\nsubmissions:\n%s",
                id, name, guildName, guildTag,
                submissions.stream().map(OldSubmission::toString).collect(Collectors.joining("\n")));
    }
}
