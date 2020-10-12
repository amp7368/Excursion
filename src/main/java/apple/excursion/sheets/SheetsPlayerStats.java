package apple.excursion.sheets;

import apple.excursion.ExcursionMain;
import apple.excursion.utils.Pair;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetsPlayerStats {
    private static final String MISSIONS_ROW_RANGE = "PlayerStats!E2:2";
    private static final String ID_COL_RANGE = "PlayerStats!A5:A";
    private static final String PLAYER_STATS_SHEET = "PlayerStats";
    private static final String BASE_PLAYER_STATS_RANGE = "E5";
    private static final String EVERYONE_RANGE = "PlayerStats";
    private static final String TASKS_DONE_ROW = "2";
    private static final int PLAYER_STATS_SHEET_ID = 0;

    private static final String TOTAL_EP_EARNED_FORMULA = "=SUM(C%d:%s)";
    public static final String TASKS_DONE_HEADER = "Tasks Done";
    private static final String TOTAL_TASKS_FORMULA = "=COUNT(C%d:%s)*1/98";

    public static boolean isQuest(String quest) {
        quest = quest.toLowerCase();
        ValueRange missionsValueRange;
        try {
            missionsValueRange = SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, MISSIONS_ROW_RANGE).execute();
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

    public static void verifyDiscordNickname(String nickname, long id) {
//        try {
//            int row = getRowFromDiscord(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, String.valueOf(id));
//            if (row == -1) {
//                addProfile(id, nickname, nextRow);
//                row = getRowFromDiscord(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, String.valueOf(id));
//                setFormulas(SheetsConstants.sheetsValues, row);
//            }
//            String cell = addA1Notation("B5", 0, row);
//            ValueRange valueRange = SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, String.format("%s!%s", PLAYER_STATS_SHEET, cell)).execute();
//
//            List<List<Object>> currentValue = valueRange.getValues();
//            if (!currentValue.isEmpty()) {
//                List<Object> currentValueInside = currentValue.get(0);
//                if (!currentValueInside.isEmpty()) {
//                    if (currentValueInside.get(0).equals(nickname))
//                        return; // nickname is already good
//                }
//            }
//
//            List<List<Object>> valueToWrite = new ArrayList<>();
//            List<Object> insideArray = new ArrayList<>();
//            insideArray.add(nickname);
//            valueToWrite.add(insideArray);
//            valueRange.setValues(valueToWrite);
//            SheetsConstants.sheetsValues.update(SheetsConstants.spreadsheetId, String.format("%s!%s", PLAYER_STATS_SHEET, cell), valueRange).setValueInputOption("USER_ENTERED").execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private static void setFormulas(Sheets.Spreadsheets.Values sheetsValues, int row) throws IOException {
        final List<List<Object>> headerValues = sheetsValues.get(SheetsConstants.spreadsheetId, MISSIONS_ROW_RANGE).execute().getValues();
        int totalEpEarnedCol = -1;
        int totalTasksCol = -1;
        for (List<Object> headerValuesInside : headerValues) {
            int headerValuesLength = headerValuesInside.size();
            for (int i = 0; i < headerValuesLength; i++) {
                Object headerValueInside = headerValuesInside.get(i);
                if (headerValueInside.equals(TASKS_DONE_HEADER)) {
                    totalEpEarnedCol = i;
                    totalTasksCol = i + 1;
                }
            }
        }
        if (totalEpEarnedCol == -1) {
            // couldn't find the correct headers
            throw new IOException("bad headers");
        }
        // we have the correct cols
        String totalEpEarnedRange = SheetsUtils.addA1Notation(BASE_PLAYER_STATS_RANGE, totalEpEarnedCol, row);
        String totalTasksRange = SheetsUtils.addA1Notation(BASE_PLAYER_STATS_RANGE, totalTasksCol, row);
        String lastRange = SheetsUtils.addA1Notation(totalEpEarnedRange, -1, 0);
        List<List<Object>> formulaValues = Collections.singletonList(Arrays.asList(
                String.format(TOTAL_EP_EARNED_FORMULA, row + 5, lastRange),
                String.format(TOTAL_TASKS_FORMULA, row + 5, lastRange)
        ));
        String formulasRange = String.format("%s!%s:%s", PLAYER_STATS_SHEET, totalEpEarnedRange, totalTasksRange);
        ValueRange formulasValueRange = new ValueRange();
        formulasValueRange.setRange(formulasRange);
        formulasValueRange.setValues(formulaValues);
        sheetsValues.update(SheetsConstants.spreadsheetId, formulasRange, formulasValueRange).setValueInputOption("USER_ENTERED").execute();
    }

    public static void submit(String spreadsheetId, Sheets.Spreadsheets.Values values, String submissionName, long discordId, String discordName) {
        try {
            Pair<String, Integer> cellToUpdate = getCellToUpdate(spreadsheetId, values, submissionName, discordId, discordName);
            if (cellToUpdate == null) {
                System.err.println("could not get the cell to update");
                return;
            }
            ValueRange valueRange = values.get(spreadsheetId, cellToUpdate.getKey()).execute();
            List<List<Object>> currentValue = valueRange.getValues();
            int oldValue = 0;
            if (currentValue != null && !currentValue.isEmpty()) {
                List<Object> currentValueInside = currentValue.get(0);
                if (!currentValueInside.isEmpty()) {
                    Object currentValueInsideInside = currentValueInside.get(0);
                    if (currentValueInsideInside instanceof String) {
                        try {
                            oldValue = Integer.parseInt((String) currentValueInsideInside);
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (currentValueInsideInside instanceof Integer) {
                        oldValue = (Integer) currentValueInsideInside;
                    }
                }
            }
            List<List<Object>> valueToWrite = new ArrayList<>();
            List<Object> insideArray = new ArrayList<>();
            insideArray.add(cellToUpdate.getValue() + oldValue);
            valueToWrite.add(insideArray);
            valueRange.setValues(valueToWrite);
            values.update(spreadsheetId, cellToUpdate.getKey(), valueRange).setValueInputOption("USER_ENTERED").execute();
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
        List<List<Object>> sheet = SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, PLAYER_STATS_SHEET).execute().getValues();
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
        String range = String.format("%s!%s:%s", PLAYER_STATS_SHEET, SheetsUtils.addA1Notation("A1", 0, lastRow), SheetsUtils.addA1Notation("A1", lastCol + 3, lastRow));
        valueRange.setRange(range);
        List<Object> profileRow = new ArrayList<>();
        profileRow.add(String.valueOf(discordId));
        profileRow.add(discordName);
        profileRow.add("");
        profileRow.add("");
        for (i = 3; i < lastCol; i++)
            profileRow.add("");
        final String profileStart = SheetsUtils.addA1Notation("A1", 4, lastRow);
        final String profileEnd = SheetsUtils.addA1Notation("A1", lastCol, lastRow);
        profileRow.add(String.format("=COUNT(%s:%s)&\" done\"", profileStart, profileEnd));
        profileRow.add(String.format("=SUM(%s:%s)", profileStart, profileEnd));
        profileRow.add(String.format("=COUNT(%s:%s)*1/121", profileStart, profileEnd));
        List<List<Object>> values = Collections.singletonList(profileRow);
        valueRange.setValues(values);
        System.out.println(range);
        ExcursionMain.service.spreadsheets().batchUpdate(SheetsConstants.spreadsheetId,
                new BatchUpdateSpreadsheetRequest().setRequests(
                        Collections.singletonList(
                                new Request().setInsertRange(
                                        new InsertRangeRequest().setRange(
                                                new GridRange().setSheetId(PLAYER_STATS_SHEET_ID)
                                                        .setStartColumnIndex(0)
                                                        .setEndColumnIndex(lastCol)
                                                        .setStartRowIndex(lastRow)
                                                        .setEndRowIndex(lastRow+1)
                                        ).setShiftDimension("ROWS")
                                )
                        )
                )
        ).execute();
        SheetsConstants.sheetsValues.update(SheetsConstants.spreadsheetId, range, valueRange).setValueInputOption("USER_ENTERED").execute();
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
        return SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, EVERYONE_RANGE).execute().getValues();
    }

    public static void rename(int row, String realName) {
        String cell = SheetsUtils.addA1Notation("B1", 0, row);
        final String range = String.format("%s!%s", PLAYER_STATS_SHEET, cell);
        ValueRange valueRange = new ValueRange().setRange(range).setValues(Collections.singletonList(Collections.singletonList(realName)));
        try {
            SheetsConstants.sheetsValues.update(SheetsConstants.spreadsheetId, range, valueRange).setValueInputOption("USER_ENTERED").execute();
        } catch (IOException ignored) {
        }
    }
}
