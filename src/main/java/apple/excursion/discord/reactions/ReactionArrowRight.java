package apple.excursion.discord.reactions;

import apple.excursion.discord.data.PageableMessages;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ReactionArrowRight implements DoReaction {

    @Override
    public void dealWithReaction(MessageReactionAddEvent event) {
        if (PageableMessages.forward(event.getMessageIdLong())) {
            User user = event.getUser();
            if (user != null)
                event.getReaction().removeReaction(user).queue();

        }
    }
}
