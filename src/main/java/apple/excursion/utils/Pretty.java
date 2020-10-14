package apple.excursion.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Pretty {
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

    public static String date(Long epochSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
                formatter.applyPattern("h:mm a 'EST' 'on' EEE, MMMMMMMMM d");
        return formatter.format(new Date(epochSeconds*1000));
    }
}
