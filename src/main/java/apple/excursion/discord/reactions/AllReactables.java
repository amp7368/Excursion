package apple.excursion.discord.reactions;


import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllReactables {
    private static final long STOP_WATCHING_DIFFERENCE = 1000 * 60 * 20; // 20 minutes
    private static final Map<Long, ReactableMessage> pageableMessages = new HashMap<>();
    private static final Object mapSyncObject = new Object();

    public static void add(ReactableMessage message) {
        synchronized (mapSyncObject) {
            pageableMessages.put(message.getId(), message);
        }
    }

    public static void remove(long id) {
        synchronized (mapSyncObject) {
            pageableMessages.remove(id);
        }
    }

    public synchronized static void dealWithReaction(@NotNull MessageReactionAddEvent event) {
        String reaction = event.getReactionEmote().getName();
        for (Reactable reactable : Reactable.values()) {
            if (reactable.isEmoji(reaction)) {
                ReactableMessage message = pageableMessages.get(event.getMessageIdLong());
                if (message != null) {
                    message.dealWithReaction(reactable, reaction, event);
                    trimOldMessages();
                    return;
                }
            }
        }
        trimOldMessages();
    }

    private static void trimOldMessages() {
        synchronized (mapSyncObject) {
            pageableMessages.values().removeIf(msg -> System.currentTimeMillis() - msg.getLastUpdated() > STOP_WATCHING_DIFFERENCE);
        }
    }

    public enum Reactable {
        LEFT(Collections.singletonList("\u2B05")),
        RIGHT(Collections.singletonList("\u27A1")),
        TOP(Collections.singletonList("\u21A9")),
        ACCEPT(Collections.singletonList("\u2705")),
        REJECT(Collections.singletonList("\u274C"));


        private final List<String> emojis;

        Reactable(List<String> emojis) {
            this.emojis = emojis;
        }

        public boolean isEmoji(String reaction) {
            return emojis.contains(reaction);
        }

        public String getFirstEmoji() {
            return emojis.get(0); // it should always have at least one emoji. otherwise it would be useless
        }
    }
}
