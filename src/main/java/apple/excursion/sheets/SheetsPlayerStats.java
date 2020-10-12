package apple.excursion.sheets;

import apple.excursion.utils.Pair;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetsPlayerStats {
    private static final String MISSIONS_ROW_RANGE = "PlayerStats!E2:2";
    private static final String ID_COL_RANGE = "PlayerStats!A5:A";
    private static final String PLAYER_INFO_RANGE = "PlayerStats!A5:D";
    private static final String PLAYER_STATS_SHEET = "PlayerStats";
    private static final String BASE_PLAYER_STATS_RANGE = "E5";

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
        try {
            int row = getRowFromDiscord(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, String.valueOf(id));
            if (row == -1) {
                addNewRow(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, String.valueOf(id), nickname);
                row = getRowFromDiscord(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, String.valueOf(id));
                setFormulas(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, row);
            }
            String cell = addA1Notation("B5", 0, row);
            ValueRange valueRange = SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, String.format("%s!%s", PLAYER_STATS_SHEET, cell)).execute();

            List<List<Object>> currentValue = valueRange.getValues();
            if (!currentValue.isEmpty()) {
                List<Object> currentValueInside = currentValue.get(0);
                if (!currentValueInside.isEmpty()) {
                    if (currentValueInside.get(0).equals(nickname))
                        return; // nickname is already good
                }
            }

            List<List<Object>> valueToWrite = new ArrayList<>();
            List<Object> insideArray = new ArrayList<>();
            insideArray.add(nickname);
            valueToWrite.add(insideArray);
            valueRange.setValues(valueToWrite);
            SheetsConstants.sheetsValues.update(SheetsConstants.spreadsheetId, String.format("%s!%s", PLAYER_STATS_SHEET, cell), valueRange).setValueInputOption("USER_ENTERED").execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setFormulas(String spreadsheetId, Sheets.Spreadsheets.Values sheetsValues, int row) throws IOException {
        final List<List<Object>> headerValues = sheetsValues.get(spreadsheetId, MISSIONS_ROW_RANGE).execute().getValues();
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
        String totalEpEarnedRange = addA1Notation(BASE_PLAYER_STATS_RANGE, totalEpEarnedCol, row);
        String totalTasksRange = addA1Notation(BASE_PLAYER_STATS_RANGE, totalTasksCol, row);
        String lastRange = addA1Notation(totalEpEarnedRange, -1, 0);
        List<List<Object>> formulaValues = Collections.singletonList(Arrays.asList(
                String.format(TOTAL_EP_EARNED_FORMULA, row + 5, lastRange),
                String.format(TOTAL_TASKS_FORMULA, row + 5, lastRange)
        ));
        String formulasRange = String.format("%s!%s:%s", PLAYER_STATS_SHEET, totalEpEarnedRange, totalTasksRange);
        ValueRange formulasValueRange = new ValueRange();
        formulasValueRange.setRange(formulasRange);
        formulasValueRange.setValues(formulaValues);
        sheetsValues.update(spreadsheetId, formulasRange, formulasValueRange).setValueInputOption("USER_ENTERED").execute();
        int a = 3;
    }

    public static void submit(String spreadsheetId, Sheets.Spreadsheets.Values values, String submissionName, String discordId, String discordName) {
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

    private static Pair<String, Integer> getCellToUpdate(String spreadsheetId, Sheets.Spreadsheets.Values values, String submissionName, String discordId, String discordName) throws IOException {
        ValueRange missionsValueRange = values.get(spreadsheetId, MISSIONS_ROW_RANGE).execute();
        List<Object> missionValues = missionsValueRange.getValues().get(0);
        // find the submission id
        int submissionIndex = -1;
        int missionValuesLength = missionValues.size();
        for (int i = 0; i < missionValuesLength; i++) {
            if (missionValues.get(i).toString().toLowerCase().equals(submissionName.toLowerCase())) {
                submissionIndex = i;
                break;
            }
        }
        if (submissionIndex == -1) {
            // could not find a quest with that name
            return null;
        }
        int idIndex = getRowFromDiscord(spreadsheetId, values, discordId);
        if (idIndex == -1) {
            // could not find a person with that id
            addNewRow(spreadsheetId, values, discordId, discordName);
            idIndex = getRowFromDiscord(spreadsheetId, values, discordId);
            return null;
        }
        String cellRange = addA1Notation(BASE_PLAYER_STATS_RANGE, submissionIndex, idIndex);

        String cellWithValueRange = addA1Notation(BASE_PLAYER_STATS_RANGE, submissionIndex, -2);
        cellWithValueRange = String.format("%s!%s", PLAYER_STATS_SHEET, cellWithValueRange);
        ValueRange newValueRange = values.get(spreadsheetId, cellWithValueRange).execute();
        Object newValueObject = newValueRange.getValues().get(0).get(0);
        int newValue;
        try {
            newValue = Integer.parseInt((String) newValueObject);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            newValue = -1;
        }
        return new Pair<>(String.format("%s!%s", PLAYER_STATS_SHEET, cellRange), newValue);

    }

    private static void addNewRow(String spreadsheetId, Sheets.Spreadsheets.Values sheets, String discordId, String discordName) throws IOException {
        ValueRange valueRange = new ValueRange();
        valueRange.setRange(PLAYER_INFO_RANGE);
        List<List<Object>> values = Collections.singletonList(Arrays.asList(discordId, discordName, "", ""));
        valueRange.setValues(values);

        sheets.append(spreadsheetId, PLAYER_INFO_RANGE, valueRange).setValueInputOption("USER_ENTERED").setInsertDataOption("INSERT_ROWS").execute();
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

    public static String addA1Notation(String base, int col, int row) {
        char[] baseChar = base.toCharArray();
        int baseCol = 0;
        int i = 0;
        int baseCharLength = baseChar.length;
        for (; i < baseCharLength && Character.isLetter(baseChar[i]); i++) {
            baseCol *= 26;
            baseCol += baseChar[i] - 64;
        }
        StringBuilder baseRow = new StringBuilder();
        for (; i < baseCharLength && Character.isDigit(baseChar[i]); i++) {
            baseRow.append(baseChar[i]);
        }
        if (!baseRow.toString().equals(""))
            row = row + Integer.parseInt(baseRow.toString());
        return getExcelColumnName(col + baseCol) + row;
    }

    private static String getExcelColumnName(int columnNumber) {
        int dividend = columnNumber;
        StringBuilder columnName = new StringBuilder();
        int modulo;

        while (dividend > 0) {
            modulo = (dividend - 1) % 26;
            columnName.insert(0, ((char) (65 + modulo)));
            dividend = (dividend - modulo) / 26;
        }

        return columnName.toString();
    }

    public static List<List<Object>> getQuestsList() {
        try {
            return SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, MISSIONS_ROW_RANGE).execute().getValues();
        } catch (IOException e) {
            return null;
        }
    }

    public static List<List<Object>> getPlayerQuestsList(String discordId) {
        try {
            int row = getRowFromDiscord(SheetsConstants.spreadsheetId, SheetsConstants.sheetsValues, discordId);
            return SheetsConstants.sheetsValues.get(SheetsConstants.spreadsheetId, String.format("%s!E%d:%d", PLAYER_STATS_SHEET, row + 5, row + 5)).execute().getValues();
        } catch (IOException e) {
            return null;
        }
    }
}
