package apple.excursion.discord.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AllChannelListeners {
    private static final long STOP_WATCHING_DIFFERENCE = 1000 * 60 * 20; // 20 minutes
    private static final Map<Long, ChannelListener> listeners = new HashMap<>();
    private static final Object mapSyncObject = new Object();

    public static void add(ChannelListener listener) {
        synchronized (mapSyncObject) {
            listeners.put(listener.getId(), listener);
        }
    }

    public static void remove(long id) {
        synchronized (mapSyncObject) {
            listeners.remove(id);
        }
    }

    public synchronized static void dealWithMessage(@NotNull MessageReceivedEvent event) {
        long channelId = event.getChannel().getIdLong();
        synchronized (mapSyncObject) {
            trimOldMessages(); // trim old messages before in case someone wasn't expecting me to be listening
            ChannelListener listener = listeners.get(channelId);
            if (listener != null)
                listener.dealWithMessage(event);
        }
    }

    private static void trimOldMessages() {
        synchronized (mapSyncObject) {
            listeners.values().removeIf(msg -> System.currentTimeMillis() - msg.getLastUpdated() > STOP_WATCHING_DIFFERENCE);
        }
    }
}
