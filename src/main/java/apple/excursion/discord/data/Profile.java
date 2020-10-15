package apple.excursion.discord.data;

import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.GetFromObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Profile {
    private static final int TOP_TASKS_SIZE = 5;
    private List<TaskSimple> tasksNotDone = new ArrayList<>();
    private List<TaskSimpleCompleted> tasksDone = new ArrayList<>();
    private int totalEp = 0;
    private int soulJuice = 0;
    private long discordId;
    private int row;
    private String name;
    private String guild;
    private String guildTag;
    private boolean complete = false;

    public Profile(Iterator<Object> profileRow, int row) {
        this.row = row;

        if (!profileRow.hasNext()) return;
        discordId = GetFromObject.getLong(profileRow.next());
        if (GetFromObject.longFail(discordId))
            return;

        if (!profileRow.hasNext()) return;
        Object next = profileRow.next();
        if (next == null) return;
        name = next.toString();

        if (!profileRow.hasNext()) return;
        next = profileRow.next();
        if (next == null || next.toString().isBlank()) soulJuice = 0;
        else {
            soulJuice = GetFromObject.getInt(next);
            if (GetFromObject.intFail(soulJuice))
                return;
        }
        if (!profileRow.hasNext()) return;
        next = profileRow.next();
        if (next == null) return;
        guild = next.toString();

        if (!profileRow.hasNext()) return;
        next = profileRow.next();
        if (next == null) return;
        guildTag = next.toString();

        complete = true;
    }

    public Profile(String name, long id, int row, Profile other) {
        this.discordId = id;
        this.row = row;
        this.name = name;
        this.guild = "";
        this.guildTag = "";
        this.tasksNotDone.addAll(other.tasksDone);
        this.tasksNotDone.addAll(other.tasksNotDone);
    }

    public void updateName(String name) {
        SheetsPlayerStats.rename(row, name);
    }

    public void addNotDone(TaskSimple task) {
        this.tasksNotDone.add(task);
    }

    public void addDone(TaskSimpleCompleted task) {
        this.tasksDone.add(task);
        this.totalEp += task.pointsEarned;
    }

    public boolean isFail() {
        return !complete;
    }

    /**
     * @param name the name to get but lowercase
     * @return true if this profile matches. otherwise false
     */
    public boolean hasName(String name) {
        return this.name.toLowerCase().equals(name);
    }

    public boolean nameContains(String name) {
        return this.name.toLowerCase().contains(name);
    }

    public boolean hasId(long id) {
        return this.discordId == id;
    }

    public int getTotalEp() {
        return totalEp;
    }

    public String getName() {
        return name;
    }

    public String getGuild() {
        return guild;
    }

    public String getGuildTag() {
        return guildTag;
    }

    public long getId() {
        return this.discordId;
    }

    public double getProgress() {
        final int tasksDoneSize = tasksDone.size();
        return ((double) tasksDoneSize) / (tasksNotDone.size() + tasksDoneSize);
    }

    public int getCountTasksDone() {
        return tasksDone.size();
    }

    public int getCountTasksTotal() {
        return tasksDone.size() + tasksNotDone.size();
    }

    public List<TaskSimple> getTopTasks(String taskType) {
        taskType = taskType.toLowerCase();
        List<TaskSimple> topTasks = new ArrayList<>();
        tasksNotDone.sort((o1, o2) -> o2.points - o1.points);
        for (TaskSimple task : tasksNotDone) {
            if (task.category.equals(taskType)) {
                topTasks.add(task);
                if (topTasks.size() == TOP_TASKS_SIZE)
                    return topTasks;
            }
        }
        return topTasks;
    }

    public int getTaskPoints(String questName) {
        for (TaskSimpleCompleted task : tasksDone) {
            if (task.name.toLowerCase().equals(questName.toLowerCase())) {
                return task.pointsEarned;
            }
        }
        return 0;
    }

    public int getRow() {
        return row;
    }
}
