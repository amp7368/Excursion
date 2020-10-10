package apple.excursion.discord.reactions;

import apple.excursion.discord.data.Submissions;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public class ReactionExit implements DoReaction {
    @Override
    public void dealWithReaction(MessageReactionAddEvent event) {
        // the emote is a check mark already
        Submissions.markAsDone(event.getMessageIdLong(),event.getUser(),false);
    }
}
