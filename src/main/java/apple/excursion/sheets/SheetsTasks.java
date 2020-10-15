package apple.excursion.sheets;

import apple.excursion.discord.data.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static apple.excursion.sheets.SheetsConstants.*;

public class SheetsTasks {
    private static final String TASKS_SPREADSHEET = "Tasks";

    public static List<Task> getTasks() {
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
        return tasks;
    }
}
