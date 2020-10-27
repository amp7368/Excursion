package apple.excursion.database.objects;

public class CrossChatId {
    public long serverId;
    public long channelId;

    public CrossChatId(long serverId, long channelId) {
        this.serverId = serverId;
        this.channelId = channelId;
    }

    @Override
    public int hashCode() {
        return (int) ((serverId % Integer.MAX_VALUE + channelId % Integer.MAX_VALUE) % Integer.MAX_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CrossChatId && serverId == ((CrossChatId) obj).serverId && channelId == ((CrossChatId) obj).channelId;
    }
}
