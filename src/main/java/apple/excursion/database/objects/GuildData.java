package apple.excursion.database.objects;

import java.util.List;

public class GuildData {
    public final List<OldSubmission> submissions;
    public String tag;
    public String name;

    public GuildData(String tag, String name, List<OldSubmission> submissions) {
        this.tag = tag;
        this.name = name;
        this.submissions = submissions;
        if (submissions != null)
            this.submissions.sort((o1, o2) -> (int) (o2.dateSubmitted - o1.dateSubmitted));
    }
}
