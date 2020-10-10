package apple.excursion.discord.data;

import java.util.HashMap;
import java.util.Map;

public class PageableMessages {
    private static final long STOP_WATCHING_DIFFERENCE = 1000 * 60 * 60; // 1 hour
    private static Map<Long, Pageable> pageableMessages = new HashMap<>();
    private static final Object mapSyncObject = new Object();

    public static void add(Pageable message) {
        synchronized (mapSyncObject) {
            pageableMessages.put(message.getId(), message);
        }
    }

    public static boolean forward(Long id) {
        synchronized (mapSyncObject) {
            Pageable message = pageableMessages.getOrDefault(id, null);
            if (message != null) {
                message.forward();
                trimOldMessages();
                return true;
            }
            trimOldMessages();
        }
        return false;
    }

    public static boolean backward(Long id) {
        synchronized (mapSyncObject) {
            Pageable message = pageableMessages.getOrDefault(id, null);
            if (message != null) {
                message.backward();
                trimOldMessages();
                return true;
            }
            trimOldMessages();
        }
        return false;
    }

    private static void trimOldMessages() {
        synchronized (mapSyncObject) {
            boolean tryAgain = true;
            while (tryAgain) {
                tryAgain = false;
                for (Long id : pageableMessages.keySet()) {
                    Pageable message = pageableMessages.get(id);
                    if (System.currentTimeMillis() - message.getLastUpdated() > STOP_WATCHING_DIFFERENCE) {
                        pageableMessages.remove(id);
                        tryAgain = true;
                        break;
                    }
                }
            }

        }
    }
}
