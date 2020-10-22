package apple.excursion.discord.commands;

import java.util.Iterator;
import java.util.List;

public class DetermineArguments {
    public static ArgumentInt determineInt(String prefix, List<String> contentSplit) {
        Iterator<String> content = contentSplit.iterator();
        while (content.hasNext()) {
            String c = content.next();
            if (c.equalsIgnoreCase(prefix)) {
                content.remove();
                if (content.hasNext()) {
                    try {
                        int val = Integer.parseInt(content.next());
                        content.remove();
                        return new ArgumentInt(val);
                    } catch (NumberFormatException e) {
                        return new ArgumentInt(true);
                    }
                }
                return new ArgumentInt(true);
            }
        }
        return new ArgumentInt(false);
    }

    public static class ArgumentInt {
        public final boolean exists;
        public final boolean hasValue;
        public final int value;

        public ArgumentInt(boolean b) {
            exists = b;
            hasValue = false;
            value = -1;
        }

        public ArgumentInt(int val) {
            hasValue = exists = true;
            value = val;
        }
    }
}
