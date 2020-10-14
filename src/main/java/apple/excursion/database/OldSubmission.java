package apple.excursion.database;


public class OldSubmission {
    public int id;
    public Long dateSubmitted;
    public String taskName;
    public String[] links;
    public String submitter;
    public String[] otherSubmitters;

    public OldSubmission(int id, Long date, String taskName, String links, String submitter, String otherSubmitters) {
        this.id = id;
        this.dateSubmitted = date;
        this.taskName = taskName;
        this.links = links == null ? null : links.split(",");
        this.submitter = submitter;
        this.otherSubmitters = otherSubmitters == null ? null : otherSubmitters.split(",");
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
