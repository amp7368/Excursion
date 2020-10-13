package apple.excursion.sheets;

import apple.excursion.ExcursionMain;
import com.google.api.services.sheets.v4.Sheets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SheetsConstants {
    public static final String SPREADSHEET_ID;
    public static Sheets.Spreadsheets.Values SHEETS_VALUES = ExcursionMain.service.spreadsheets().values();

    static {
        List<String> list = Arrays.asList(apple.excursion.ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        String SHEET_ID_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/config/sheetId.data";
        File file = new File(SHEET_ID_FILE_PATH);
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException ignored) {
            }
            System.err.println("Please fill in the id for the sheet in '" + SHEET_ID_FILE_PATH + "'");
            System.exit(1);
        }
        String tempSpreadsheetId = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            tempSpreadsheetId = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Please fill in the id for the sheet in '" + SHEET_ID_FILE_PATH + "'");
            System.exit(1);
        }
        SPREADSHEET_ID = tempSpreadsheetId;
    }
}
