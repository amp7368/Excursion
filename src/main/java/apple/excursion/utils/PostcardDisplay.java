package apple.excursion.utils;

import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.messages.postcard.PostcardListMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PostcardDisplay {
    public static MessageEmbed getMessage(Task task) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(task.name + " - (" + task.points + " EP)");
        embed.setAuthor(Pretty.upperCaseFirst(task.category));
        embed.setDescription(task.description);
        if (task.createdBy != null)
            embed.setFooter("Keep in mind each bullet point (-) is a task. You can make a task submission per bullet point.\n\nThis task was created by " +
                    task.createdBy + String.format(" - Repeatable %s time%s",
                    task.bulletsCount == -1 ? "\u221E" : String.valueOf(task.bulletsCount), task.bulletsCount == 1 ? "" : "s"));
        else
            embed.setFooter("Keep in mind each bullet point (-) is a task. You can make a task submission per bullet point.");
        embed.setColor(PostcardListMessage.Category.valueOf(task.category.toUpperCase()).color);
        return embed.build();
    }
}
