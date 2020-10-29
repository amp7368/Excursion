package apple.excursion.database.objects;

import javax.annotation.Nullable;
import java.util.List;

public class CrossChatMessage {

    public final List<MessageId> messageIds;
    public final long myMessageId;
    public final long owner;
    public final String username;
    public final int color;
    public final String avatarUrl;
    public String description;
    public String reactions;
    @Nullable
    public final String imageUrl;

    public CrossChatMessage(List<MessageId> messageIds, long myMessageId, long owner, String username, int color, String avatarUrl, @Nullable String imageUrl, String description, String reactions) {
        this.messageIds = messageIds;
        this.myMessageId = myMessageId;
        this.owner = owner;
        this.username = username;
        this.color = color;
        this.avatarUrl = avatarUrl;
        this.imageUrl = imageUrl;
        this.description = description;
        this.reactions = reactions;
    }
}
