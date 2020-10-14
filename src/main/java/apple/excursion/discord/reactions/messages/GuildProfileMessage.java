package apple.excursion.discord.reactions.messages;

import apple.excursion.database.objects.GuildData;
import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.PlayerData;
import apple.excursion.discord.data.answers.GuildLeaderboardProfile;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;

public class GuildProfileMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 15;
    private final GuildData matchedGuild;
    private final List<PlayerData> players;
    private final GuildLeaderboardProfile guildProfile;
    private final Message message;
    private int page = 0;
    private long lastUpdated = System.currentTimeMillis();

    public GuildProfileMessage(GuildData matchedGuild, List<PlayerData> players, GuildLeaderboardProfile guildProfile, MessageChannel channel) {
        this.matchedGuild = matchedGuild;
        this.players = players;
        this.guildProfile = guildProfile;
        players.sort((o1, o2) -> o2.score - o1.score);
        this.message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private Message makeMessage() {
        MessageBuilder message = new MessageBuilder();
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%s [%s] Leaderboards Page (%d)\n", matchedGuild.name, matchedGuild.tag, page));
        text.append(getDash());
        text.append(String.format("|%3s| %-31s| %-8s|\n", "", "Name", "Total EP"));
        int upper = Math.min(((page + 1) * ENTRIES_PER_PAGE), players.size());
        for (int place = page * ENTRIES_PER_PAGE; place < upper; place++) {
            if (place % 5 == 0) {
                text.append(getDash());
            }
            PlayerData player = players.get(place);
            text.append(String.format("|%3s| %-31s| %-8s|", place + 1, player.name, player.score));
            text.append("\n");
        }
        text.append("```");

        message.setContent(text.toString());


        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("%s [%s]", matchedGuild.name, matchedGuild.tag));
        StringBuilder description = new StringBuilder();
        description.append(String.format("Guild rank : #%d\n\n", guildProfile.getRank()));
        description.append(Pretty.getProgressBar(guildProfile.getProgress()));
        description.append("\n\n");
        description.append(String.format("Guild EP: %d EP", guildProfile.getTotalEp()));
        description.append("\n\n");
        if (matchedGuild.submissions.isEmpty()) {
            description.append("**There is no submission history**\n");
        } else {
            description.append("**Submission History:** \n");
            for (OldSubmission submission : matchedGuild.submissions) {
                description.append(submission.makeSubmissionHistoryMessage());
                description.append('\n');
            }
        }
        embed.setDescription(description.toString());
        message.setEmbed(embed.build());
        return message.build();
    }

    private String getDash() {
        return "-".repeat(40) + "\n";
    }

    public void forward() {
        if ((players.size() - 1) / ENTRIES_PER_PAGE >= page + 1) {
            page++;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    public void backward() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        final User user = event.getUser();
        if (user == null) return;
        switch (reactable) {
            case LEFT:
                backward();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                forward();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                top();
                event.getReaction().removeReaction(user).queue();
                break;
        }
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }
}
