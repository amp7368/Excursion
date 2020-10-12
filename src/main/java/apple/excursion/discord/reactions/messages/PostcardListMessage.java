package apple.excursion.discord.reactions.messages;

import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
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
        dares.removeIf(task -> !task.category.equals(Category.DARES.toString().toLowerCase()));
        excursions.removeIf(task -> !task.category.equals(Category.EXCURSIONS.toString().toLowerCase()));
        missions.removeIf(task -> !task.category.equals(Category.MISSIONS.toString().toLowerCase()));

        this.message = channel.sendMessage(makeMessage()).complete();
    }

    private String makeMessage() {
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%s List page (%d)\n", currentCategory.name(), page));
        text.append(String.format("%-23s| %-7s| %s\n", "Name", "EP", "Description"));
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
            text.append(String.format("%c: %-20s| %-7s| %s\n", (char) (65 + i),
                    task.taskName,
                    task.ep,
                    task.description.length() > 28 ? task.description.substring(0, 25) + "..." : task.description
            ));
        }
        text.append("```");
        return text.toString();

    }

    @Override
    public void dealWithReaction(AllReactables.Reactable reactable, String reaction, MessageReactionAddEvent event) {

    }

    @Override
    public Long getId() {
        return message.getIdLong();
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
