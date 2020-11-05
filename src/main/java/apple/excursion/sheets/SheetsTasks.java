package apple.excursion.sheets;

import apple.excursion.discord.data.Task;
import apple.excursion.utils.SendLogs;
import com.google.api.services.sheets.v4.model.ValueRange;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static apple.excursion.sheets.SheetsConstants.*;

public class SheetsTasks extends Thread {
    private static final String TASKS_SPREADSHEET = "Tasks";
    private static final long HOUR = 1000 * 60 * 60;
    private static List<Task> tasks = new ArrayList<>();

    public static List<Task> getTasks() {
        return tasks;
    }

    @Nullable
    public static Task getTaskWithName(String taskName) {
        Pattern pattern = Pattern.compile(".*" + taskName + ".*", Pattern.CASE_INSENSITIVE);
        for (Task task : tasks) {
            if (pattern.matcher(task.name).matches()) return task;
        }
        return null;
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
