package apple.excursion.discord.reactions.messages;

import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostcardListMessage implements ReactableMessage {
    private static final int ENTRIES_PER_PAGE = 9;
    private final Message message;
    private final List<Task> allTasks = SheetsTasks.getTasks();
    private final List<Task> dares;
    private final List<Task> excursions;
    private final List<Task> missions;

    private long lastUpdated = System.currentTimeMillis();
    private Category currentCategory = Category.ALL;
    private int page = 0;

    public PostcardListMessage(MessageChannel channel) {
        dares = new ArrayList<>(allTasks);
        excursions = new ArrayList<>(allTasks);
        missions = new ArrayList<>(allTasks);
        dares.removeIf(task -> !task.category.equals(Category.DARES.name().toLowerCase()));
        excursions.removeIf(task -> !task.category.equals(Category.EXCURSIONS.name().toLowerCase()));
        missions.removeIf(task -> !task.category.equals(Category.MISSIONS.name().toLowerCase()));
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

    private String makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%s List page (%d)\n", currentCategory.name(), page));
        text.append(String.format("%-23s| %-4s| %s\n", "Name", "EP", "Description"));
        List<Task> tasks;
        switch (currentCategory) {
            case ALL:
                tasks = allTasks;
                break;
            case DARES:
                tasks = dares;
                break;
            case MISSIONS:
                tasks = missions;
                break;
            case EXCURSIONS:
                tasks = excursions;
                break;
            default:
                tasks = Collections.emptyList();
                break;
        }
        int upper = Math.min((page + 1) * ENTRIES_PER_PAGE, tasks.size());
        for (int i = page * ENTRIES_PER_PAGE; i < upper; i++) {
            Task task = tasks.get(i);
            text.append(String.format("%c: %-20s| %-4s| %s\n", (char) (65 + i),
                    task.taskName,
                    task.ep,
                    task.description.length() > 38 ?
                            task.description.replaceAll("\n", " ").substring(0, 35) + "..." :
                            task.description.replaceAll("\n", " ")
            ));
        }
        text.append("```");
        return text.toString();

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
                setCategory(Category.EXCURSIONS);
                event.getReaction().removeReaction(user).queue();
                break;
            case MISSIONS:
                setCategory(Category.MISSIONS);
                event.getReaction().removeReaction(user).queue();
                break;
            case DARES:
                setCategory(Category.DARES);
                event.getReaction().removeReaction(user).queue();
                break;
            case ALL_CATEGORIES:
                setCategory(Category.ALL);
                event.getReaction().removeReaction(user).queue();
                break;
            case ALPHABET:

                event.getReaction().removeReaction(user).queue();
                break;
        }
    }

    private void setCategory(Category category) {
        this.currentCategory = category;
        this.page = 0;
        message.editMessage(makeMessage()).queue();
        this.lastUpdated = System.currentTimeMillis();
    }

    public void forward() {
        List<Task> tasks;
        switch (currentCategory) {
            case ALL:
                tasks = allTasks;
                break;
            case DARES:
                tasks = dares;
                break;
            case MISSIONS:
                tasks = missions;
                break;
            case EXCURSIONS:
                tasks = excursions;
                break;
            default:
                tasks = Collections.emptyList();
                break;
        }
        if ((page + 1) * ENTRIES_PER_PAGE < tasks.size()) {
            ++page;
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
            message.editMessage(makeMessage()).queue();
            this.lastUpdated = System.currentTimeMillis();
        }
    }

    @Override
    public long getLastUpdated() {
        return lastUpdated;
    }

    private enum Category {
        DARES(AllReactables.Reactable.DARES),
        EXCURSIONS(AllReactables.Reactable.EXCURSIONS),
        ALL(AllReactables.Reactable.ALL_CATEGORIES),
        MISSIONS(AllReactables.Reactable.MISSIONS);

        public final AllReactables.Reactable reactable;

        Category(AllReactables.Reactable reactable) {
            this.reactable = reactable;
        }
    }
}
