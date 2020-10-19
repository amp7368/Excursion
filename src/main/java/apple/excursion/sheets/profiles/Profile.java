package apple.excursion.sheets.profiles;


import apple.excursion.discord.data.TaskSimple;
import apple.excursion.discord.data.TaskSimpleCompleted;
import apple.excursion.utils.GetFromObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Profile {
    private int totalEp = 0;
    private int soulJuice = 0;
    private long discordId;
    private String name;
    private String guild;
    private String guildTag;
    private boolean complete = false;
    private int row;
    private List<TaskSimple> tasksNotDone = new ArrayList<>();
    public List<TaskSimpleCompleted> tasksDone = new ArrayList<>();

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

    public void addNotDone(TaskSimple task) {
        this.tasksNotDone.add(task);
    }

    public void addDone(TaskSimpleCompleted task) {
        this.tasksDone.add(task);
        this.totalEp += task.pointsEarned;
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

    public int getSoulJuice() {
        return soulJuice;
    }

    @Override
    public int hashCode() {
        return (int) (discordId % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Profile && ((Profile) other).hasId(discordId);
    }
}