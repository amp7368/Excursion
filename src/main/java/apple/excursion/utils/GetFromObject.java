package apple.excursion.utils;

import java.util.Iterator;

public class GetFromObject {

    private static final int INT_FAIL = Integer.MIN_VALUE;
    private static final long LONG_FAIL = Long.MIN_VALUE;

    /**
     * converts an object to an int
     *
     * @param o the object we're converting
     * @return INT_FAIL if a fail, otherwise o as an int
     */
    public static int getInt(Object o) {
        if (o == null) return INT_FAIL;
        if (o instanceof Integer) return (Integer) o;
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return INT_FAIL;
        }
    }

    public static long getLong(Object o) {
        if (o == null) return LONG_FAIL;
        if (o instanceof Long) return (Long) o;
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return LONG_FAIL;
        }
    }

    public static boolean intFail(int i) {
        return i == INT_FAIL;
    }

    public static boolean longFail(long i) {
        return i == LONG_FAIL;
    }

}
