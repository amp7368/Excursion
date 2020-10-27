package apple.excursion.discord.cross_chat;

import apple.excursion.database.objects.CrossChatId;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.queries.InsertDB;
import apple.excursion.discord.DiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrossChat {
    private static final Object sync = new Object();
    private static final Map<CrossChatId, MessageChannel> crossChats = new HashMap<>();

    static {
        try {
            List<CrossChatId> crossChatsList = GetDB.getDiscordChannels();
            for (CrossChatId crossChat : crossChatsList) {
                crossChats.put(crossChat, DiscordBot.client.getTextChannelById(crossChat.channelId));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            System.exit(1);
        }
    }

    public static void add(CrossChatId crossChat) {
        synchronized (sync) {
            crossChats.put(crossChat, DiscordBot.client.getTextChannelById(crossChat.channelId));
        }
    }

    public static void add(long serverId, long discordId, MessageChannel channel) {
        try {
            InsertDB.insertCrossChat(serverId, discordId);
            channel.sendMessage("This channel has been linked to the Excursion discords").queue();
        } catch (SQLException throwables) {
            channel.sendMessage("There was an SQLException linking your discord. Inform appleptr16#5054 if the issue persists.").queue();
        }
    }

    public static void remove(long serverId, MessageChannel channel) {
        synchronized (sync) {
            try {
                InsertDB.removeCrossChat(serverId);
                CrossChatId matched = null;
                for (CrossChatId crossChat : crossChats.keySet()) {
                    if (crossChat.serverId == serverId) {
                        matched = crossChat;
                        break;
                    }
                }
                if (matched == null) {
                    channel.sendMessage("Your server wasn't linked.").queue();
                } else {
                    crossChats.remove(matched);
                    channel.sendMessage("Your server's discord has been unlinked with the other discords.").queue();
                }
            } catch (SQLException throwables) {
                channel.sendMessage("There was an SQLException unlinking your discord. Inform appleptr16#5054 if the issue persists.").queue();
            }
        }
    }

    public static void dealWithMessage(MessageReceivedEvent event) {
        long serverId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        for (CrossChatId crossChat : crossChats.keySet()) {
            if (crossChat.channelId == channelId && crossChat.serverId == serverId) {
                Member member = event.getMember();
                if (member == null) break;
                String username = member.getEffectiveName();
                int color = member.getColorRaw();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(color);
                embedBuilder.setAuthor(username, null, event.getAuthor().getAvatarUrl());
                embedBuilder.setDescription(event.getMessage().getContentDisplay());
                MessageEmbed builtMessage = embedBuilder.build();
                for (Map.Entry<CrossChatId, MessageChannel> crossChatToSend : crossChats.entrySet()) {
                    if (!crossChatToSend.getKey().equals(crossChat))
                        crossChatToSend.getValue().sendMessage(builtMessage).queue();
                }
                break;
            }
        }
    }
}
