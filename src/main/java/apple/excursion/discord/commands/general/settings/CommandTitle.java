package apple.excursion.discord.commands.general.settings;

import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import apple.excursion.utils.Pretty;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandTitle implements DoCommand {
    private static final List<Title> titles = Arrays.asList(
            new Title("Unique", 10),
            new Title("Ragnic", 10),
            new Title("Deltian", 10),
            new Title("Mercenary", 10),
            new Title("Mason", 10),
            new Title("Legendary", 20),
            new Title("Almujian", 20),
            new Title("Llevigarian", 20),
            new Title("Oluxen", 20),
            new Title("Dame", 20),
            new Title("Knight", 20),
            new Title("Fabled", 30),
            new Title("Tromsian", 30),
            new Title("Cinfresque", 30),
            new Title("Rymekish", 30),
            new Title("Hunter", 30),
            new Title("Huntress", 30),
            new Title("Mythical", 40),
            new Title("Dwarven", 40),
            new Title("Aldoresque", 40),
            new Title("Corkian", 40),
            new Title("Prince", 40),
            new Title("Princess", 40),
            new Title("Wybel", 50),
            new Title("Luthonian", 50),
            new Title("Olmic", 50),
            new Title("Ahmsordese", 50),
            new Title("Emperor", 50),
            new Title("Empress", 50)
    );
    private static final List<ColorRole> colors = Arrays.asList(
            new ColorRole("Excursion Orange", 10, 727023199161679912L),
            new ColorRole("Mission Mustard", 10, 728080112532324363L),
            new ColorRole("Dare Red", 10, 727021694761566311L),
            new ColorRole("Rare Magenta", 20, 728080119012393031L),
            new ColorRole("Unique Yellow", 20, 744702524640198727L),
            new ColorRole("Set Green", 20, 744702664201470022L),
            new ColorRole("Legendary Blue", 30, 744469914508853258L),
            new ColorRole("Crafting Green", 30, 728080122258653316L),
            new ColorRole("Bucketlist Indigo", 30, 728080120761417828L),
            new ColorRole("Mythic Purple", 40, 744702853398003794L)
    );


    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] content = event.getMessage().getContentStripped().split(" ");
        ColoredName coloredName = GetColoredName.get(event.getAuthor().getIdLong());
        String name;
        if ((name = coloredName.getName()) == null) {
            Member member = event.getMember();
            if (member == null) return;
            name = ColoredName.getGuestName(member.getEffectiveName());
        }
        PlayerData playerData;
        try {
            playerData = GetDB.getPlayerData(new Pair<>(event.getAuthor().getIdLong(), name));
        } catch (SQLException throwables) {
            event.getChannel().sendMessage("There has been an SQLException getting your player data").queue();
            return;
        }
        int tasksDone = playerData.submissions.size();
        if (content.length < 2) {
            EmbedBuilder helpEmbed = new EmbedBuilder();
            helpEmbed.setTitle("Titles and Colors available for " + name);
            StringBuilder description = new StringBuilder();
            description.append("**You've unlocked these titles:**");
            boolean isTitlesAvailable = false;
            int lastReq = -1;
            for (Title title : titles) {
                if (title.req <= tasksDone) {
                    if (title.req != lastReq) {
                        lastReq = title.req;
                        description.append("\n");
                        description.append(String.format("%s", title.title));
                    } else {
                        description.append(String.format(" **\u2022** %s", title.title));
                    }
                    isTitlesAvailable = true;
                }
            }
            if (!isTitlesAvailable) {
                description.append("You have no titles available right now.\n");
            }

            description.append("\n\n");
            description.append("**You've unlocked these colors**");
            lastReq = -1;
            boolean isColorsAvailable = false;
            for (ColorRole color : colors) {
                if (color.req <= tasksDone) {
                    if (color.req != lastReq) {
                        lastReq = color.req;
                        description.append(String.format("\n%s", color.color));
                    } else {
                        description.append(String.format(" **\u2022** %s", color.color));
                    }
                    isColorsAvailable = true;
                }
            }
            if (!isColorsAvailable) {
                description.append("You have no colors available right now.\n");
            }
            helpEmbed.setDescription(description.toString());
            helpEmbed.setImage("https://cdn.discordapp.com/attachments/743394724567711745/747884178493669509/info3.png"); // colors image
            helpEmbed.setColor(coloredName.getColor());
            event.getChannel().sendMessage(helpEmbed.build()).queue();
        } else {
            Member member = event.getMember();
            if (member == null) return;
            Guild guild = member.getGuild();
            if (guild.getIdLong() != DiscordBot.EXCURSION_GUILD_ID) {
                event.getChannel().sendMessage(String.format("You need to be in (and execute the command in) the The Farplane discord server <%s> to have a custom title or color", DiscordBot.EXCURSION_GUILD_INVITE)).queue();
                return;
            }

            // the player tried to change their title or color
            List<String> contentList = new ArrayList<>(Arrays.asList(content));
            contentList.remove(0);
            String titleOrColor = String.join(" ", contentList);
            Title matchedTitle = null;
            for (Title title : titles) {
                if (title.title.equalsIgnoreCase(titleOrColor)) {
                    matchedTitle = title;
                    break;
                }
            }
            if (matchedTitle == null) {
                // try to find the color
                ColorRole matchedColor = null;
                for (ColorRole colorRole : colors) {
                    if (colorRole.color.equalsIgnoreCase(titleOrColor)) {
                        matchedColor = colorRole;
                        break;
                    }
                }
                if (matchedColor == null) {
                    event.getChannel().sendMessage(String.format("There is no title or color with name of '%s'", titleOrColor)).queue();
                } else {
                    if (tasksDone < matchedColor.req) {
                        event.getChannel().sendMessage(String.format("You've completed %d/%d tasks required to earn this title.", tasksDone, matchedColor.req)).queue();
                        return;
                    }
                    Role role = member.getGuild().getRoleById(matchedColor.role);
                    if (role == null) {
                        event.getChannel().sendMessage("I couldn't get that role. Let someone know.").queue();
                        return;
                    }
                    try {
                        List<Role> userRoles = member.getRoles();
                        for (ColorRole color : colors) {
                            for (Role r : userRoles) {
                                if (color.role == r.getIdLong()) {
                                    guild.removeRoleFromMember(member, r).queue();
                                    break;
                                }
                            }
                        }
                        guild.addRoleToMember(member, role).queue();
                    } catch (PermissionException e) {
                        event.getChannel().sendMessage("I don't have permission to change your role D:").queue();
                        return;
                    }
                    event.getChannel().sendMessage("Your color has been updated to " + matchedColor.color).queue();
                }
            } else {
                // try to change the player's title
                if (tasksDone < matchedTitle.req) {
                    event.getChannel().sendMessage(String.format("You've completed %d/%d tasks required to earn this title.", tasksDone, matchedTitle.req)).queue();
                    return;
                }
                try {
                    member.modifyNickname(matchedTitle.title + " " + Pretty.upperCaseFirst(event.getAuthor().getName())).queue();
                } catch (PermissionException e) {
                    event.getChannel().sendMessage("I don't have permission to change your nickname D:").queue();
                    return;
                }
                event.getChannel().sendMessage("Your title has been updated to " + matchedTitle.title).queue();
            }

        }
    }

    private static class ColorRole {
        private final String color;
        private final int req;
        private final long role;

        public ColorRole(String color, int req, long role) {
            this.color = color;
            this.req = req;
            this.role = role;
        }
    }

    private static class Title {
        private final String title;
        private final int req;

        public Title(String title, int req) {
            this.title = title;
            this.req = req;
        }
    }
}
