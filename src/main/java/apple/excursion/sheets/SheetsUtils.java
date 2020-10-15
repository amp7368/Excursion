package apple.excursion.sheets;

public class SheetsUtils {
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
}
