package apple.excursion.discord.data;

import apple.excursion.sheets.SheetsPlayerStats;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    public List<String> tasksNotDone;
    public List<String> tasksDone;
    public int totalEp;
    public String name;
    public String guild;
    public String guildTag;

    public Profile(long profileId) {
        tasksDone = new ArrayList<>();
        tasksNotDone = new ArrayList<>();

        List<List<Object>> questsListBig = SheetsPlayerStats.getQuestsList();
        List<List<Object>> playerQuestsListBig = SheetsPlayerStats.getPlayerQuestsList(String.valueOf(profileId));

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
            if (questNameObject.equals(SheetsPlayerStats.TASKS_DONE_HEADER)) {
                break;
            }
            if (playerQuestObject == null || playerQuestObject.equals("")) {
                tasksNotDone.add(questNameObject.toString());
            } else if (playerQuestObject instanceof String) {
                try {
                    int questXp = Integer.parseInt((String) playerQuestObject);
                    totalEp += questXp;
                    tasksDone.add(questNameObject.toString());
                } catch (NumberFormatException e) {
                    tasksNotDone.add(questNameObject.toString());
                }
            } else if (playerQuestObject instanceof Integer) {
                totalEp += (int) playerQuestObject;
                tasksDone.add(questNameObject.toString());
            } else
                tasksNotDone.add(questNameObject.toString());
        }
    }
}
