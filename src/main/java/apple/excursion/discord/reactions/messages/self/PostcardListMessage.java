package apple.excursion.discord.reactions.messages.self;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.PostcardDisplay;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostcardListMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 9;
    private Message message;
    private final List<Task> allTasks;
    private List<Task> dares;
    private List<Task> excursions;
    private List<Task> missions;

    private long lastUpdated = System.currentTimeMillis();
    private Category currentCategory = Category.ALL;
    private int page = 0;

    private int taskLookingAt = -1;

    public PostcardListMessage(MessageChannel channel) {
        allTasks = SheetsTasks.getTasks();
        initialize(channel);
    }

    private void initialize(MessageChannel channel) {
        dares = new ArrayList<>(allTasks);
        excursions = new ArrayList<>(allTasks);
        missions = new ArrayList<>(allTasks);
        dares.removeIf(task -> !task.category.equalsIgnoreCase(Category.DARE.name()));
        excursions.removeIf(task -> !task.category.equalsIgnoreCase(Category.EXCURSION.name()));
        missions.removeIf(task -> !task.category.equalsIgnoreCase(Category.MISSION.name()));
        dares.sort((t1, t2) -> t2.ep - t1.ep);
        excursions.sort((t1, t2) -> t2.ep - t1.ep);
        missions.sort((t1, t2) -> t2.ep - t1.ep);
        allTasks.sort((t1, t2) -> t2.ep - t1.ep);


        this.message = channel.sendMessage(makeMessage()).complete();
        this.message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        this.message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        this.message.addReaction(DiscordBot.client.getEmoteById(AllReactables.Reactable.DARES.getFirstId())).queue();
        this.message.addReaction(DiscordBot.client.getEmoteById(AllReactables.Reactable.EXCURSIONS.getFirstId())).queue();
        this.message.addReaction(DiscordBot.client.getEmoteById(AllReactables.Reactable.MISSIONS.getFirstId())).queue();
        int i = 0;
        for (String letter : AllReactables.emojiAlphabet) {
            if (i++ == ENTRIES_PER_PAGE) break;
            this.message.addReaction(letter).queue();
        }
        AllReactables.add(this);
    }

    public PostcardListMessage(MessageChannel channel, List<Task> allTasks) {
        this.allTasks = allTasks;
        initialize(channel);
    }

    private String makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%s List page (%d)\n", Pretty.upperCaseFirst(currentCategory.name()), page + 1));
        text.append(String.format("%-27s| %-4s| %s\n", "Name", "EP", "Category"));
        List<Task> tasks = getCurrentTasks();
        int upper = Math.min((page + 1) * ENTRIES_PER_PAGE, tasks.size());
        for (int i = page * ENTRIES_PER_PAGE, j = 0; i < upper; i++, j++) {
            Task task = tasks.get(i);
            text.append(String.format("%c: %-24s| %-4s| %s\n", (char) (65 + j),
                    task.taskName,
                    task.ep,
                    Pretty.upperCaseFirst(task.category)
            ));
        }
        text.append("```");
        return text.toString();

    }

    private List<Task> getCurrentTasks() {
        List<Task> tasks;
        switch (currentCategory) {
            case ALL:
                tasks = allTasks;
                break;
            case DARE:
                tasks = dares;
                break;
            case MISSION:
                tasks = missions;
                break;
            case EXCURSION:
                tasks = excursions;
                break;
            default:
                tasks = Collections.emptyList();
                break;
        }
        return tasks;
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
            case EXCURSIONS:
                setCategory(Category.EXCURSION);
                event.getReaction().removeReaction(user).queue();
                break;
            case MISSIONS:
                setCategory(Category.MISSION);
                event.getReaction().removeReaction(user).queue();
                break;
            case DARES:
                setCategory(Category.DARE);
                event.getReaction().removeReaction(user).queue();
                break;
            case ALL_CATEGORIES:
                setCategory(Category.ALL);
                event.getReaction().removeReaction(user).queue();
                break;
            case ALPHABET:
                event.getReaction().removeReaction(user).queue();
                this.lastUpdated = System.currentTimeMillis();
                for (int i = 0; i < AllReactables.emojiAlphabet.size(); i++) {
                    if (AllReactables.emojiAlphabet.get(i).equals(event.getReactionEmote().getName())) {
                        // this is the reaction we're looking at
                        this.lastUpdated = System.currentTimeMillis();

                        if (taskLookingAt == i) {
                            taskLookingAt = -1;
                            message.editMessage(makeMessage()).queue();
                            return;
                        }

                        List<Task> tasks = getCurrentTasks();
                        if (i >= ENTRIES_PER_PAGE || i >= tasks.size()) {
                            // they reacted too high
                            return;
                        }
                        message.editMessage(PostcardDisplay.getMessage(tasks.get(i))).queue();
                        taskLookingAt = i;
                    }
                }
                break;
        }
    }

    private void setCategory(Category category) {
        this.currentCategory = this.currentCategory == category ? Category.ALL : category;
        this.page = 0;
        this.taskLookingAt = -1;
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    public void forward() {
        List<Task> tasks = getCurrentTasks();
        if ((page + 1) * ENTRIES_PER_PAGE < tasks.size()) {
            ++page;
            taskLookingAt = -1;
            message.editMessage(makeMessage()).queue();
        } else if (taskLookingAt != -1) {
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public Long getId() {
        return message.getIdLong();
    }

    public void backward() {
        if (page - 1 != -1) {
            --page;
            taskLookingAt = -1;
            message.editMessage(makeMessage()).queue();
        } else if (taskLookingAt != -1) {
            message.editMessage(makeMessage()).queue();
        }
        this.lastUpdated = System.currentTimeMillis();
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    public enum Category {
        DARE(0xff9797),
        EXCURSION(0xfaab5f),
        ALL(0xffffff),
        MISSION(0xc3993e);

        public int color;

        Category(int color) {
            this.color = color;
        }
    }
}
