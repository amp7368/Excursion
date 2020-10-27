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
}
