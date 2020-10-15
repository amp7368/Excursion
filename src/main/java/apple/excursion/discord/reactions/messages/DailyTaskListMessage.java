package apple.excursion.discord.reactions.messages;

import apple.excursion.database.GetCalendarDB;
import apple.excursion.discord.data.answers.DailyTaskWithDate;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.discord.reactions.ReactableMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class DailyTaskListMessage implements ReactableMessage {
    private final Message message;
    Calendar calendar = Calendar.getInstance();
    private long lastUpdated = System.currentTimeMillis();

    public DailyTaskListMessage(MessageChannel channel) {
        message = channel.sendMessage(makeMessage()).complete();
        message.addReaction(AllReactables.Reactable.LEFT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.RIGHT.getFirstEmoji()).queue();
        message.addReaction(AllReactables.Reactable.TOP.getFirstEmoji()).queue();
        AllReactables.add(this);
    }

    private String makeMessage() {
        List<DailyTaskWithDate> week = null;
        try {
            week = GetCalendarDB.getWeek(calendar);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if (week == null)
            return "Something went wrong"; // todo give better message
        StringBuilder text = new StringBuilder();
        text.append("```glsl\n");
        text.append(String.format("%20s   %-23s%s\n", "", "Daily Tasks", "EP"));
        for (DailyTaskWithDate day : week) {
            text.append("\n");
            text.append(String.format("%s %d %s\n", day.dayOfWeek, day.dayOfMonth, day.month));
            for (String task : day.tasks) {
                text.append(String.format("%20s   %-23s%s\n", "", task, "###"));
            }
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
                calendar.add(Calendar.WEEK_OF_YEAR, -1);
                lastUpdated = System.currentTimeMillis();
                message.editMessage(makeMessage()).queue();
                event.getReaction().removeReaction(user).queue();
                break;
            case RIGHT:
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                lastUpdated = System.currentTimeMillis();
                message.editMessage(makeMessage()).queue();
                event.getReaction().removeReaction(user).queue();
                break;
            case TOP:
                calendar = Calendar.getInstance();
                lastUpdated = System.currentTimeMillis();
                message.editMessage(makeMessage()).queue();
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
