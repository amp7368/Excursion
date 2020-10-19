package apple.excursion.utils;

import apple.excursion.discord.DiscordBot;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.*;

public class GetColoredName {
    private static Collection<Long> roles = new HashSet<>();

    static {
        roles.add(555342151135920138L); // Excusionist
        roles.add(757738058656252034L); // Masonry
        roles.add(555340987086667776L); // Farplane Resident
        roles.add(570490957959790593L); // Buke
        roles.add(570096950695821332L); // Shogunate
        roles.add(570069084591358000L); // Regent
        roles.add(555342023180288002L); // Yako Dynasty
    }

    public static ColoredName get(long id) {
        Guild guild = DiscordBot.client.getGuildById(DiscordBot.EXCURSION_GUILD_ID);
        if (guild == null) return new ColoredName();
        Member member = guild.getMemberById(id);
        if (member == null) {
            return new ColoredName();
        } else {
            String name = member.getEffectiveName();
            List<Role> memberRoles = member.getRoles();
            for (Role role : memberRoles) {
                if (roles.contains(role.getIdLong())) {
                    return new ColoredName(name, role.getColorRaw());
                }
            }
            return new ColoredName(name);
        }
    }

    public static List<Long> get(String nameToGet) {
        List<Long> ids = new ArrayList<>();
        Guild guild = DiscordBot.client.getGuildById(DiscordBot.EXCURSION_GUILD_ID);
        if (guild == null) return ids;
        nameToGet = nameToGet.toLowerCase();
        List<Member> members = guild.getMembers();
        for (Member member : members) {
            if (member.getEffectiveName().toLowerCase().contains(nameToGet))
                ids.add(member.getIdLong());
        }
        return ids;
    }
}
