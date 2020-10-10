package apple.excursion.discord.data;

import apple.excursion.sheets.PlayerStats;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public List<String> questsNotDone;
    public List<String> questsDone;
    public int totalEp;

    public Profile(long profileId) {
        questsDone = new ArrayList<>();
        questsNotDone = new ArrayList<>();

        List<List<Object>> questsListBig = PlayerStats.getQuestsList();
        List<List<Object>> playerQuestsListBig = PlayerStats.getPlayerQuestsList(String.valueOf(profileId));

        if (questsListBig == null || playerQuestsListBig == null || questsListBig.isEmpty() || playerQuestsListBig.isEmpty()) {
            totalEp = -1;
            return;
        }

        List<Object> questsList = questsListBig.get(0);
        List<Object> playerQuestsList = playerQuestsListBig.get(0);

        int questsListLength = questsList.size();

        for (int i = 0; i < questsListLength; i++) {
            Object questNameObject = questsList.get(i);
            Object playerQuestObject = playerQuestsList.get(i);
            if (questNameObject.equals(PlayerStats.TOTAL_EP_EARNED_HEADER))
                break;
            if (playerQuestObject == null || playerQuestObject.equals("")) {
                questsNotDone.add(questNameObject.toString());
            } else if (playerQuestObject instanceof String) {
                try {
                    int questXp = Integer.parseInt((String) playerQuestObject);
                    totalEp += questXp;
                    questsDone.add(questNameObject.toString());
                } catch (NumberFormatException e) {
                    questsNotDone.add(questNameObject.toString());
                }
            } else if (playerQuestObject instanceof Integer) {
                totalEp += (int) playerQuestObject;
                questsDone.add(questNameObject.toString());
            } else
                questsNotDone.add(questNameObject.toString());
        }
    }
}
