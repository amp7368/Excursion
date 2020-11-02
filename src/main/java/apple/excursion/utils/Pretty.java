package apple.excursion.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pretty {
    private static final int NUM_OF_CHARS_PROGRESS = 20;

    public static String upperCaseFirst(String s) {
        String[] split = s.split(" ");
        for (int i = 0; i < split.length; i++) {
            char[] chars = split[i].toCharArray();
            if (chars.length != 0)
                chars[0] = Character.toUpperCase(chars[0]);
            for (int j = 1; j < chars.length; j++) {
                chars[j] = Character.toLowerCase(chars[j]);
            }
            split[i] = new String(chars);
        }
        return String.join(" ", split);
    }

    public static String date(Long epochMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("h:mm a 'EST' 'on' EEE, MMMMMMMMM d");
        return formatter.format(new Date(epochMilliseconds));
    }

    public static Object dateShort(long epochMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("MMM d yyyy");
        return formatter.format(new Date(epochMilliseconds));
    }

    public static String getProgressBar(double percentage) {
        StringBuilder result = new StringBuilder();
        int length = (int) (percentage * NUM_OF_CHARS_PROGRESS);
        result.append("\u2588".repeat(Math.max(0, length)));
        length = NUM_OF_CHARS_PROGRESS - length;
        result.append("\u2591".repeat(Math.max(0, length)));
        return result.toString();
    }
}
