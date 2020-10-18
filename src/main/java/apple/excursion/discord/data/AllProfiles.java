package apple.excursion.discord.data;

import apple.excursion.database.objects.guild.LeaderboardOfGuilds;
import apple.excursion.database.objects.player.PlayerLeaderboard;
import apple.excursion.sheets.SheetsPlayerStats;
import apple.excursion.utils.GetFromObject;
import apple.excursion.utils.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class AllProfiles {
    private static List<Profile> profiles = new ArrayList<>();
    private static final Object profileSync = new Object();

    public static void update() {
        synchronized (profileSync) {
            profiles = new ArrayList<>();
            List<List<Object>> everyone;
            try {
                everyone = SheetsPlayerStats.getEveryone();
            } catch (IOException e) {
                return;
            }
            if (everyone == null || everyone.size() < 3) return;

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
                if (category == null){
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
                        if (GetFromObject.intFail(pointsEarnedByPlayer) || pointsEarnedByPlayer == 0) {
                            // the player has not done this task
                            profileTemp.getValue().addNotDone(new TaskSimple(questEp, questName, category));
                        } else {
                            profileTemp.getValue().addDone(new TaskSimpleCompleted(questEp, questName, category, pointsEarnedByPlayer));
                        }
                    }
                }
            }
            profiles.removeIf(Profile::isFail);
        }
    }

    public static List<Profile> getProfile(String nameToGet) {
        nameToGet = nameToGet.toLowerCase();
        List<Profile> answers = new ArrayList<>();
        synchronized (profileSync) {
            for (Profile profile : profiles) {
                if (profile.nameContains(nameToGet)) {
                    answers.add(profile);
                }
            }
        }
        return answers;
    }

    @Nullable
    public static Profile getProfile(long id, String name) {
        synchronized (profileSync) {
            for (Profile profile : profiles) {
                if (profile.hasId(id)) {
                    if (!profile.hasName(name.toLowerCase()))
                        profile.updateName(name);
                    return profile;
                }
            }
            //make a new profile
            try {
                int row = SheetsPlayerStats.addProfile(id, name);
                if (profiles.isEmpty()) return null;
                final Profile newProfile = new Profile(name, id, row, profiles.get(0));
                profiles.add(newProfile);
                return newProfile;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static PlayerLeaderboard getOverallLeaderboard() {
        List<Profile> leaderboard = getCopy();
        leaderboard.sort((o1, o2) -> o2.getTotalEp() - o1.getTotalEp());
        return null;
    }

    public static LeaderboardOfGuilds getLeaderboardOfGuilds() {
        List<Profile> leaderboard = getCopy();
        return new LeaderboardOfGuilds(null);
    }

    @NotNull
    private static List<Profile> getCopy() {
        List<Profile> leaderboard;
        synchronized (profileSync) {
            leaderboard = new ArrayList<>(profiles);
        }
        return leaderboard;
    }

}
