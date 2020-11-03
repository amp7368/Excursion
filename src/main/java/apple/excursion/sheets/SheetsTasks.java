package apple.excursion.sheets;

import apple.excursion.discord.data.Task;
import apple.excursion.utils.SendLogs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static apple.excursion.sheets.SheetsConstants.*;

public class SheetsTasks extends Thread {
    private static final String TASKS_SPREADSHEET = "Tasks";
    private static final long HOUR = 1000 * 60 * 60;
    private static List<Task> tasks = new ArrayList<>();

    public static List<Task> getTasks() {
        return tasks;
    }

    @Override
    public void run() {
        try {
            while (true) {
                List<List<Object>> values;
                try {
                    values = SHEETS_VALUES.get(SPREADSHEET_ID, TASKS_SPREADSHEET).execute().getValues();
                } catch (IOException e) {
                    values = Collections.emptyList();
                }
                List<Task> tasks = new ArrayList<>();
                for (List<Object> row : values) {
                    tasks.add(new Task(row));
                }
                tasks.removeIf(task -> task.isFail);
                SheetsTasks.tasks = tasks;
                try {
                    Thread.sleep(HOUR);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SendLogs.error("Tasks", "There was a catastophic error and now the TaskUpdated has failed");
        }
    }
}
