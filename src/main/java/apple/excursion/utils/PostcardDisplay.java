package apple.excursion.utils;

import apple.excursion.discord.data.Task;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.File;
import java.net.MalformedURLException;

public class PostcardDisplay {
    public static MessageEmbed getMessage(Task task) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(task.taskName);
        embed.setDescription("**" + Pretty.upperCaseFirst(task.category) + "**\n" + task.description);
//        try {
//            embed.setImage(new File("C:/img.png").toURI().toURL().replace("file:","attachment:"));
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
        return embed.build();
    }
}
