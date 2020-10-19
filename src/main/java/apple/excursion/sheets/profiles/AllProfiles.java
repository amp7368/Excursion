package apple.excursion.sheets.profiles;

import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.GetFromObject;
import apple.excursion.utils.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AllProfiles {
    public static List<Profile> getProfiles() throws IOException {
        List<Profile> profiles = new ArrayList<>();
        List<List<Object>> everyone;
        everyone = SheetsPlayerStats.getEveryone();
        if (everyone == null || everyone.size() < 3) throw new IOException("nothing is in the sheet");

        List<Object> questNameCategoryList = everyone.get(0);
        List<Object> questNameList = everyone.get(1);
        List<Object> questEpList = everyone.get(2);
        List<List<Object>> profileRowList = everyone.subList(4, everyone.size());

        Iterator<Object> questNameCategoriesIterator = questNameCategoryList.iterator();
        Iterator<Object> questNameIterator = questNameList.iterator();
        Iterator<Object> questEpIterator = questEpList.iterator();
        List<Iterator<Object>> profileRowIterators = profileRowList.stream().map(List::iterator).collect(Collectors.toList());
        List<Pair<Iterator<Object>, Profile>> profilesTemp = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (questNameCategoriesIterator.hasNext()) questNameCategoriesIterator.next();
            if (questNameIterator.hasNext()) questNameIterator.next();
            if (questEpIterator.hasNext()) questEpIterator.next();
        }
        int i = 4;
        for (Iterator<Object> profileRow : profileRowIterators) {
            final Profile profile = new Profile(profileRow, i++);
            profiles.add(profile);
            profilesTemp.add(new Pair<>(profileRow, profile));
        }

        String category = null;
        while (questNameIterator.hasNext() && questEpIterator.hasNext()) {

            if (questNameCategoriesIterator.hasNext()) {
                Object questNameCategory = questNameCategoriesIterator.next();
                if (questNameCategory != null && !questNameCategory.equals(""))
                    category = questNameCategory.toString();
            }
            if (category == null) {
                questNameIterator.next();
                questEpIterator.next();
                continue;
            }
            String questName = questNameIterator.next().toString();
            if (questName.equals(SheetsPlayerStats.TASKS_DONE_HEADER)) break; //we're done collecting data

            int questEp = GetFromObject.getInt(questEpIterator.next());
            if (GetFromObject.intFail(questEp)) continue;
            for (Pair<Iterator<Object>, Profile> profileTemp : profilesTemp) {
                if (profileTemp.getKey().hasNext()) {
                    int pointsEarnedByPlayer = GetFromObject.getInt(profileTemp.getKey().next());
                    if (!GetFromObject.intFail(pointsEarnedByPlayer) && pointsEarnedByPlayer != 0) {
                        profileTemp.getValue().addEp(pointsEarnedByPlayer);
                    }
                }
            }
        }
        profiles.removeIf(Profile::isFail);
        return profiles;
    }
}