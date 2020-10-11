package apple.excursion.sheets;

import java.io.IOException;
import java.util.List;

public class LeaderBoardSheet {
    private static final String EVERYONE_RANGE = "PlayerStats";

    public static List<List<Object>> getEveryone() throws IOException {
        return SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, EVERYONE_RANGE).execute().getValues();
    }
}
