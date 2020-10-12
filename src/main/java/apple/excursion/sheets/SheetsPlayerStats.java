package apple.excursion.sheets;

import apple.excursion.ExcursionMain;
import apple.excursion.discord.data.AllProfiles;
import apple.excursion.discord.data.Profile;
import apple.excursion.utils.GetFromObject;
import apple.excursion.utils.Pair;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.*;

import static apple.excursion.sheets.SheetsConstants.*;
import static apple.excursion.sheets.SheetsUtils.addA1Notation;

public class SheetsPlayerStats {
    private static final String MISSIONS_ROW_RANGE = "PlayerStats!E2:3";
    private static final String ID_COL_RANGE = "PlayerStats!A5:A";
    private static final String PLAYER_STATS_SHEET = "PlayerStats";
    private static final String BASE_PLAYER_STATS_RANGE = "E5";
    private static final String EVERYONE_RANGE = "PlayerStats";
    private static final String TASKS_DONE_ROW = "2";
    private static final int PLAYER_STATS_SHEET_ID = 0;

    public static final String TASKS_DONE_HEADER = "Tasks Done";

    public static boolean isQuest(String quest) {
        quest = quest.toLowerCase();
        ValueRange missionsValueRange;
        try {
            missionsValueRange = SHEETS_VALUES.get(SPREADSHEET_ID, MISSIONS_ROW_RANGE).execute();
        } catch (IOException e) {
            return false;
        }
        List<Object> missionValues = missionsValueRange.getValues().get(0);
        for (Object missionValue : missionValues) {
            if (missionValue instanceof String) {
                if (((String) missionValue).toLowerCase().equals(quest))
                    return true;
            }
            if (missionValue.toString().equals(quest)) {
                return true;
            }
        }
        // could not find a quest with that name
        return false;
    }

    public synchronized static void submit(String questNameToAdd, long discordId, String discordName) throws IOException, NumberFormatException {
        Profile profile = AllProfiles.getProfile(discordId, discordName);
        if (profile == null) throw new IOException("Error making a new profile");
        int pointsToAdd = -1;
        int col = -1;
        try {
            List<List<Object>> missions = SHEETS_VALUES.get(SPREADSHEET_ID, MISSIONS_ROW_RANGE).execute().getValues();
            Iterator<Object> questNameIterator = missions.get(0).iterator();
            Iterator<Object> questValuesIterator = missions.get(1).iterator();

            for (int i = 4; true; i++) {
                String questName;
                if ((questName = questNameIterator.next().toString()).equals(TASKS_DONE_HEADER)) {
                    break;
                }
                int questValue = GetFromObject.getInt(questValuesIterator.next());
                if (questName.equals(questNameToAdd)) {
                    if (GetFromObject.intFail(questValue)) {
                        throw new NumberFormatException("Bad number in mission value");
                    }
                    pointsToAdd = questValue;
                    col = i;
                    break;
                }
            }
            final String range = PLAYER_STATS_SHEET + "!" + addA1Notation(BASE_PLAYER_STATS_RANGE, col - 4, profile.getRow() - 4);

            int pointsThere;
            List<List<Object>> pointsThereRaw = SHEETS_VALUES.get(SPREADSHEET_ID, range).execute().getValues();
            if (pointsThereRaw == null) pointsThere = 0;
            else if (pointsThereRaw.get(0) == null) pointsThere = 0;
            else pointsThere = GetFromObject.getInt(pointsThereRaw.get(0).get(0));
            if (GetFromObject.intFail(pointsThere))
                throw new NumberFormatException("Bad number in player's mission value");

            SHEETS_VALUES.update(SPREADSHEET_ID, range,
                    new ValueRange().setRange(range)
                            .setValues(Collections.singletonList(Collections.singletonList(String.valueOf(pointsThere + pointsToAdd))))
            ).setValueInputOption("USER_ENTERED").execute();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    private static Pair<String, Integer> getCellToUpdate(String spreadsheetId, Sheets.Spreadsheets.Values values, String submissionName, long discordId, String discordName) throws IOException {
//        ValueRange missionsValueRange = values.get(spreadsheetId, MISSIONS_ROW_RANGE).execute();
//        List<Object> missionValues = missionsValueRange.getValues().get(0);
//        // find the submission id
//        int submissionIndex = -1;
//        int missionValuesLength = missionValues.size();
//        for (int i = 0; i < missionValuesLength; i++) {
//            if (missionValues.get(i).toString().toLowerCase().equals(submissionName.toLowerCase())) {
//                submissionIndex = i;
//                break;
//            }
//        }
//        if (submissionIndex == -1) {
//            // could not find a quest with that name
//            return null;
//        }
//        int idIndex = getRowFromDiscord(spreadsheetId, values, String.valueOf(discordId));
//        if (idIndex == -1) {
//            // could not find a person with that id
//            addProfile(discordId, discordName, nextRow);
//            idIndex = getRowFromDiscord(spreadsheetId, values, String.valueOf(discordId));
//            return null;
//        }
//        String cellRange = addA1Notation(BASE_PLAYER_STATS_RANGE, submissionIndex, idIndex);
//
//        String cellWithValueRange = addA1Notation(BASE_PLAYER_STATS_RANGE, submissionIndex, -2);
//        cellWithValueRange = String.format("%s!%s", PLAYER_STATS_SHEET, cellWithValueRange);
//        ValueRange newValueRange = values.get(spreadsheetId, cellWithValueRange).execute();
//        Object newValueObject = newValueRange.getValues().get(0).get(0);
//        int newValue;
//        try {
//            newValue = Integer.parseInt((String) newValueObject);
//        } catch (NumberFormatException exception) {
//            exception.printStackTrace();
//            newValue = -1;
//        }
//        return new Pair<>(String.format("%s!%s", PLAYER_STATS_SHEET, cellRange), newValue);
        return null;
    }

    /**
     * adds a profile to the sheet
     *
     * @param discordId   the id of the player
     * @param discordName
     * @return
     * @throws IOException
     */
    public static int addProfile(long discordId, String discordName) throws IOException {
        List<List<Object>> sheet = SHEETS_VALUES.get(SPREADSHEET_ID, PLAYER_STATS_SHEET).execute().getValues();
        int lastRow = 5;
        for (int i = sheet.size() - 1; i >= 0; i--) {
            final List<Object> row = sheet.get(i);
            if (row.size() >= 2 && row.get(1) != null && !row.get(1).toString().isBlank()) {
                lastRow = i + 1;
                break;
            }
        }
        int lastCol = -1;
        int i = 0;
        for (Object col : sheet.get(1)) {
            if (col != null && col.toString().equals(TASKS_DONE_HEADER))
                lastCol = i - 1;
            i++;
        }

        ValueRange valueRange = new ValueRange();
        String range = String.format("%s!%s:%s", PLAYER_STATS_SHEET, addA1Notation("A1", 0, lastRow), addA1Notation("A1", lastCol + 3, lastRow));
        valueRange.setRange(range);
        List<Object> profileRow = new ArrayList<>();
        profileRow.add(String.valueOf(discordId));
        profileRow.add(discordName);
        profileRow.add("");
        profileRow.add("");
        for (i = 3; i < lastCol; i++)
            profileRow.add("");
        final String profileStart = addA1Notation("A1", 4, lastRow);
        final String profileEnd = addA1Notation("A1", lastCol, lastRow);
        profileRow.add(String.format("=COUNT(%s:%s)&\" done\"", profileStart, profileEnd));
        profileRow.add(String.format("=SUM(%s:%s)", profileStart, profileEnd));
        profileRow.add(String.format("=COUNT(%s:%s)*1/121", profileStart, profileEnd));
        List<List<Object>> values = Collections.singletonList(profileRow);
        valueRange.setValues(values);
        ExcursionMain.service.spreadsheets().batchUpdate(SPREADSHEET_ID,
                new BatchUpdateSpreadsheetRequest().setRequests(
                        Collections.singletonList(
                                new Request().setInsertRange(
                                        new InsertRangeRequest().setRange(
                                                new GridRange().setSheetId(PLAYER_STATS_SHEET_ID)
                                                        .setStartColumnIndex(0)
                                                        .setEndColumnIndex(lastCol)
                                                        .setStartRowIndex(lastRow)
                                                        .setEndRowIndex(lastRow + 1)
                                        ).setShiftDimension("ROWS")
                                )
                        )
                )
        ).execute();
        SHEETS_VALUES.update(SPREADSHEET_ID, range, valueRange).setValueInputOption("USER_ENTERED").execute();
        return lastRow;
    }

    private static int getRowFromDiscord(String spreadsheetId, Sheets.Spreadsheets.Values values, String discordId) throws IOException {
        ValueRange idValueRange = values.get(spreadsheetId, ID_COL_RANGE).execute();
        List<List<Object>> idValues = idValueRange.getValues();
        int idIndex = -1;
        int idValuesLength = idValues.size();
        for (int i = 0; i < idValuesLength; i++) {
            if (idValues.get(i).isEmpty())
                continue;
            String element = idValues.get(i).get(0).toString();
            if (element.equals(discordId)) {
                idIndex = i;
                break;
            }
        }
        return idIndex;
    }

    public static List<List<Object>> getEveryone() throws IOException {
        return SHEETS_VALUES.get(SPREADSHEET_ID, EVERYONE_RANGE).execute().getValues();
    }

    public static void rename(int row, String realName) {
        String cell = addA1Notation("B1", 0, row);
        final String range = String.format("%s!%s", PLAYER_STATS_SHEET, cell);
        ValueRange valueRange = new ValueRange().setRange(range).setValues(Collections.singletonList(Collections.singletonList(realName)));
        try {
            SHEETS_VALUES.update(SPREADSHEET_ID, range, valueRange).setValueInputOption("USER_ENTERED").execute();
        } catch (IOException ignored) {
        }
    }
}
