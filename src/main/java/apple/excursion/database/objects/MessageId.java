package apple.excursion.database.objects;

public class MessageId {
    public final long serverId;
    public final long channelId;
    public final long messageId;

    public MessageId(long sId, long cId, long mId) {
        this.serverId = sId;
        this.channelId = cId;
        this.messageId = mId;
    }

    @Override
    public int hashCode() {
        int max = Integer.MAX_VALUE / 3;
        return (int) (serverId % max + channelId % max + messageId % max);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MessageId) {
            MessageId other = (MessageId) obj;
            return other.serverId == serverId
                    && other.channelId == channelId
                    && other.messageId == messageId;
        }
        return false;
    }
}
