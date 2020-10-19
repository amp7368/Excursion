package apple.excursion.utils;

import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.messages.self.PostcardListMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class PostcardDisplay {
    public static MessageEmbed getMessage(Task task) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(task.taskName + " - (" + task.ep + " EP)");
        embed.setDescription("**" + Pretty.upperCaseFirst(task.category) + "**\n" + task.description);
        embed.setColor(PostcardListMessage.Category.valueOf(task.category.toUpperCase()).color);
        return embed.build();
    }
}
