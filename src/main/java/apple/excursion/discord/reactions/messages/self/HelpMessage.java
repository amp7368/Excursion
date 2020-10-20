package apple.excursion.discord.reactions.messages.self;

import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class HelpMessage implements ReactableMessage {
    private Message message;
    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public long getLastUpdated() {
        return 0;
    }
}
