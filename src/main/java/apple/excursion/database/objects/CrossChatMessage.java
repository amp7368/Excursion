package apple.excursion.database.objects;

import java.util.List;

public class CrossChatMessage {

    public final List<MessageId> messageIds;
    public final String username;
    public final int color;
    public final String avatarUrl;
    public final String description;
    public final String reactions;

    public CrossChatMessage(List<MessageId> messageIds, String username, int color, String avatarUrl, String description, String reactions) {
        this.messageIds = messageIds;
        this.username = username;
        this.color = color;
        this.avatarUrl = avatarUrl;
        this.description = description;
        this.reactions = reactions;
    }
}
