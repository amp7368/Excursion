package apple.excursion.sheets;

import java.io.IOException;
import java.util.List;

public class LeaderBoardSheet {
    private static final String LEADER_BOARD_RANGE = "Leaderboards!D4:E";

    public static List<List<Object>> getLeaderBoard() {
        try {
            return SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, LEADER_BOARD_RANGE).execute().getValues();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
