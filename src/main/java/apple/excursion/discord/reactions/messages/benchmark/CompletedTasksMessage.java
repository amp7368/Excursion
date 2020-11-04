package apple.excursion.discord.reactions.messages.benchmark;

import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;

public class CompletedTasksMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 10;
    private final Message message;
    private final List<Pair<Task, List<OldSubmission>>> taskNameToSubmissions;
    private final PlayerData player;
    private int page = 0;
    private List<OldSubmission> currentOldSubmissions;
    private int oldSubmissionPage = 0;
    private long lastUpdated = System.currentTimeMillis();

    public CompletedTasksMessage(PlayerData player, List<Pair<Task, List<OldSubmission>>> taskNameToSubmissions, MessageChannel channel) {
        this.player = player;
        this.taskNameToSubmissions = taskNameToSubmissions;
        this.taskNameToSubmissions.sort((t1, t2) -> {
            List<OldSubmission> l2 = t2.getValue();
            List<OldSubmission> l1 = t1.getValue();
            Task task2 = t2.getKey();
            Task task1 = t1.getKey();
            boolean fallShort2 = task2.isFallsShort(l2.size());
            boolean fallShort1 = task1.isFallsShort(l1.size());
            if (fallShort1 && !fallShort2)
                return 1;
            if (fallShort2 && !fallShort1)
                return -1;
            if (!fallShort1) {
                return String.CASE_INSENSITIVE_ORDER.compare(task1.name, task2.name);
            }
            int difference = l2.size() - l1.size();
            if (difference == 0) {
                int task1bulletsCount = task1.bulletsCount;
                int task2bulletsCount = task2.bulletsCount;
                if (task1bulletsCount == -1)
                    task1bulletsCount = 999;
                if (task2bulletsCount == -1)
                    task2bulletsCount = 999;
                if (task2bulletsCount - task1bulletsCount == 0)
                    return String.CASE_INSENSITIVE_ORDER.compare(task1.name, task2.name);
                return task2bulletsCount - task1bulletsCount;
            }
            return difference;
        });
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.CLOCK_RIGHT.getFirstEmoji()).queue();
        for (int i = 0; i < ENTRIES_PER_PAGE; i++) {
            message.addReaction(AllReactables.emojiAlphabet.get(i)).queue();
        }
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
        char correspondingLetter = 'A';
        for (int lower = page * ENTRIES_PER_PAGE; lower < upper; lower++) {
            if (lower % 5 == 0) {
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
                    case SYNC:
                        normalsCount++;
                        break;
                }
            }
            int bulletsCount = thisTaskNameAndSubmissions.getKey().bulletsCount;
            text.append(String.format("%c: %-27s| %-10d| %-10d| %-8s| %-10b|\n",
                    correspondingLetter++,
                    thisTaskNameAndSubmissions.getKey().name,
                    normalsCount,
                    dailiesCount,
                    bulletsCount == -1 ? "\u221E" : String.valueOf(bulletsCount),
                    !thisTaskNameAndSubmissions.getKey().isFallsShort(normalsCount)
                    )
            );
        }
        text.append(getDash());
        text.append("\n```");
        return text.toString();
    }

    private static String getDash() {
        return "-".repeat(77) + "\n";
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
            case ALPHABET:
                alphabet(event.getReactionEmote().getEmoji());
                event.getReaction().removeReaction(user).queue();
                break;
            case CLOCK_LEFT:
                submissionBackward();
                event.getReaction().removeReaction(user).queue();
                break;
            case CLOCK_RIGHT:
                submissionForward();
                event.getReaction().removeReaction(user).queue();
                break;
        }
        lastUpdated = System.currentTimeMillis();

    }

    private void submissionForward() {
        if (currentOldSubmissions == null) return;
        if (oldSubmissionPage == currentOldSubmissions.size() - 1) return;
        oldSubmissionPage++;
        MessageEmbed embed = currentOldSubmissions.get(oldSubmissionPage).getDisplay(String.format("%d/%d", oldSubmissionPage + 1, currentOldSubmissions.size()));
        MessageBuilder newMessage = new MessageBuilder();
        newMessage.setContent(makeMessage());
        newMessage.setEmbed(embed);
        message.editMessage(newMessage.build()).queue();
    }

    private void submissionBackward() {
        if (currentOldSubmissions == null) return;
        if (oldSubmissionPage == 0) return;
        oldSubmissionPage--;
        MessageEmbed embed = currentOldSubmissions.get(oldSubmissionPage).getDisplay(String.format("%d/%d", oldSubmissionPage + 1, currentOldSubmissions.size()));
        MessageBuilder newMessage = new MessageBuilder();
        newMessage.setContent(makeMessage());
        newMessage.setEmbed(embed);
        message.editMessage(newMessage.build()).queue();
    }

    private void alphabet(String emojiClicked) {
        int c = 0;
        for (String emoji : AllReactables.emojiAlphabet) {
            if (emoji.equals(emojiClicked)) {
                c += page * ENTRIES_PER_PAGE;
                break;
            } else {
                c++;
            }
        }
        if (c > taskNameToSubmissions.size()) {
            return;
        }
        currentOldSubmissions = taskNameToSubmissions.get(c).getValue();
        if (currentOldSubmissions.isEmpty()) return;
        oldSubmissionPage = 0;
        MessageEmbed embed = currentOldSubmissions.get(oldSubmissionPage).getDisplay(String.format("%d/%d", oldSubmissionPage + 1, currentOldSubmissions.size()));
        MessageBuilder newMessage = new MessageBuilder();
        newMessage.setContent(makeMessage());
        newMessage.setEmbed(embed);
        message.editMessage(newMessage.build()).queue();
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
        message.removeReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        message.removeReaction(AllReactables.Reactable.CLOCK_LEFT.getFirstEmoji()).queue();
        message.removeReaction(AllReactables.Reactable.CLOCK_RIGHT.getFirstEmoji()).queue();
        for (int i = 0; i < ENTRIES_PER_PAGE; i++) {
            message.removeReaction(AllReactables.emojiAlphabet.get(i)).queue();
        }
    }
}
