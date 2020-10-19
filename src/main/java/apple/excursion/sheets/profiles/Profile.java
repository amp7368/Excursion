package apple.excursion.sheets.profiles;


import apple.excursion.utils.GetFromObject;

import java.util.Iterator;

public class Profile {
    private int totalEp = 0;
    private int soulJuice = 0;
    private long discordId;
    private String name;
    private String guild;
    private String guildTag;
    private boolean complete = false;
    private int row;

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

    public void addEp(int points) {
        this.totalEp += points;
    }

    public int getSoulJuice() {
        return soulJuice;
    }
}
