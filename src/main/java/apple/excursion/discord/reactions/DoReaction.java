package apple.excursion.discord.reactions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface DoReaction {
     void dealWithReaction(MessageReactionAddEvent event);
}
