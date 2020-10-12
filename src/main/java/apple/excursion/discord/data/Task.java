package apple.excursion.discord.data;

import apple.excursion.utils.GetFromObject;

import javax.annotation.Nullable;
import java.util.List;

public class Task {
    public String category; // is lowercase
    public String taskName;
    public String description;
    @Nullable
    public String coordinates;
    public int ep;
    public String createdBy;
    public String repeatable;
    @Nullable
    public String images;
    public boolean isFail = false;

    public Task(List<Object> row) {
        category = row.get(0).toString().toLowerCase();
        taskName = row.get(1).toString();
        description = row.get(2).toString();
        coordinates = row.get(3) == null ? null : row.get(0).toString();
        ep = GetFromObject.getInt(row.get(4));
        createdBy = row.get(5).toString();
        repeatable = row.get(6).toString();
        images = row.size() < 8 ? null : row.get(7).toString();
        isFail = GetFromObject.intFail(ep);
    }
}
