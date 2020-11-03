package apple.excursion.discord.reactions.messages.benchmark;

import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;
import java.util.Map;

public class CompletedTasksMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 10;
    private final Message message;
    private final List<Pair<Task, List<OldSubmission>>> taskNameToSubmissions;
    private final PlayerData player;
    private int page = 0;

    private long lastUpdated = System.currentTimeMillis();

    public CompletedTasksMessage(PlayerData player, List<Pair<Task, List<OldSubmission>>> taskNameToSubmissions, MessageChannel channel) {
        this.player = player;
        this.taskNameToSubmissions = taskNameToSubmissions;
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private String makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append("Completed Tasks for ").append(player.name).append("\n");
        text.append(getDash());
        text.append(String.format("   %-27s| %-10s| %-10s| %-8s| %-10s|\n", "Name", "Normals", "Dailies", "Task", "Fully"));
        text.append(String.format("   %-27s| %-10s| %-10s| %-8s| %-10s|\n", "", "Completed", "Completed", "Bullets", "Completed"));

        int upper = Math.min(taskNameToSubmissions.size(), (page + 1) * ENTRIES_PER_PAGE);
        char correspondingLetter = 65;
        for (int lower = page * ENTRIES_PER_PAGE; lower < upper; lower++) {
            if(lower%5==0){
                text.append(getDash());
            }
            Pair<Task, List<OldSubmission>> thisTaskNameAndSubmissions = taskNameToSubmissions.get(lower);
            int normalsCount = 0, dailiesCount = 0;
            for (OldSubmission submission : thisTaskNameAndSubmissions.getValue()) {
                switch (submission.submissionType) {
                    case DAILY:
                        dailiesCount++;
                        break;
                    case NORMAL:
                    case OLD:
                        normalsCount++;
                        break;
                }
            }
            text.append(String.format("%c: %-27s| %-10d| %-10d| %-8d| %-10b|\n",
                    correspondingLetter++,
                    thisTaskNameAndSubmissions.getKey().name,
                    normalsCount,
                    dailiesCount,
                    -1,
                    normalsCount > -1
                    )
            );
        }
        text.append(getDash());
        text.append("\n```");
        return text.toString();
    }

    private static String getDash() {
        return "-".repeat(77)+"\n";
    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {
        User user = event.getUser();
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
        lastUpdated = System.currentTimeMillis();

    }

    private void top() {
        page = 0;
        message.editMessage(makeMessage()).queue();
    }

    private void forward() {
        if ((taskNameToSubmissions.size() - 1) / ENTRIES_PER_PAGE >= page + 1) {
            page++;
            message.editMessage(makeMessage()).queue();
        }
    }

    private void backward() {
        if (page != 0) {
            page--;
            message.editMessage(makeMessage()).queue();
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

    @Override
    public void dealWithOld() {
        message.removeReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.removeReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
    }
}
