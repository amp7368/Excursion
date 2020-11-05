package apple.excursion.discord.data;

import apple.excursion.utils.GetFromObject;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
    public final String category; // is lowercase
    public final String name;
    public final String description;
    @Nullable
    public final String coordinates;
    public final int bulletsCount;
    @Nullable
    public final String creationDate;
    public int points;
    public final String createdBy;
    @Nullable
    public final String images;
    public final boolean isFail;

    public Task(List<Object> row) {
        String category = row.get(0).toString().toLowerCase();
        char[] categoryChars = category.toLowerCase().toCharArray();
        if (categoryChars.length != 0 && categoryChars[categoryChars.length - 1] == 's') {
            this.category = category.toLowerCase().substring(0, categoryChars.length - 1);
        } else
            this.category = category.toLowerCase();
        name = row.get(1).toString().trim();
        description = row.get(2).toString();
        coordinates = row.get(3) == null ? null : row.get(0).toString();
        final Object epObject = row.get(4);
        if (epObject != null) {
            final String epString = epObject.toString();
            if (epString.endsWith(" EP"))
                points = GetFromObject.getInt(epString.substring(0, epString.length() - 3));
            else
                points = GetFromObject.getInt(epObject);
        }
        createdBy = row.get(5).toString();
        bulletsCount = GetFromObject.getInt(row.get(6));
        creationDate = row.size() < 8 ? null : row.get(7).toString();
        images = row.size() < 9 ? null : row.get(8).toString();
        isFail = GetFromObject.intFail(points) || GetFromObject.intFail(bulletsCount);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Task && ((Task) other).name.equalsIgnoreCase(name);
    }

    public boolean isFallsShort(int size) {
        return bulletsCount == -1 || size < bulletsCount;
    }
}
