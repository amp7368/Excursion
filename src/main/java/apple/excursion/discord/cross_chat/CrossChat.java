package apple.excursion.discord.cross_chat;

import apple.excursion.database.VerifyDB;
import apple.excursion.database.objects.CrossChatId;
import apple.excursion.database.objects.CrossChatMessage;
import apple.excursion.database.objects.MessageId;
import apple.excursion.database.queries.GetDB;
import apple.excursion.database.queries.InsertDB;
import apple.excursion.database.queries.UpdateDB;
import apple.excursion.discord.DiscordBot;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.SendLogs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class CrossChat {
    private static final Object sync = new Object();
    private static final Map<CrossChatId, MessageChannel> crossChats = new HashMap<>();
    private static CrossChatMessage lastCrossChat = null;
    private static long lastCrossChatTime = 0;
    private static final long SPAM_TIME_DIFFERENCE = 30 * 1000;

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
            for (CrossChatId cc : crossChats.keySet()) {
                if (cc.serverId == crossChat.serverId) {
                    crossChats.remove(cc);
                    break;
                }
            }
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
        long owner = event.getAuthor().getIdLong();
        long serverId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        for (CrossChatId crossChat : crossChats.keySet()) {
            if (crossChat.channelId == channelId && crossChat.serverId == serverId) {
                if (lastCrossChat != null && owner == lastCrossChat.owner &&
                        event.getMessage().getAttachments().isEmpty() &&
                        makeReactionMessage(lastCrossChat.description, lastCrossChat.reactions).length() + event.getMessage().getContentDisplay().length() < 1900 &&
                        System.currentTimeMillis() - lastCrossChatTime < SPAM_TIME_DIFFERENCE
                ) {
                    // then update the last message
                    lastCrossChat.description += "\n" + event.getMessage().getContentDisplay();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setColor(lastCrossChat.color);
                    embedBuilder.setAuthor(lastCrossChat.username + " #" + lastCrossChat.myMessageId, null, lastCrossChat.avatarUrl);
                    embedBuilder.setDescription(makeReactionMessage(lastCrossChat.description, lastCrossChat.reactions)); // with the new description
                    if (lastCrossChat.imageUrl != null) embedBuilder.setImage(lastCrossChat.imageUrl);
                    event.getMessage().delete().queue();
                    for (MessageId messageId : lastCrossChat.messageIds) {
                        TextChannel channel = DiscordBot.client.getTextChannelById(messageId.channelId);
                        if (channel == null) continue; // it's fine if the channel is null just skip editing this msg
                        channel.retrieveMessageById(messageId.messageId).queue(
                                message -> {
                                    message.editMessage(embedBuilder.build()).queue();
                                }, failure -> {
                                }//ignore a fail
                        );
                    }

                    // make the update in the DB
                    try {
                        UpdateDB.updateCrossChatDescription(lastCrossChat);
                    } catch (SQLException ignored) {
                        // ignore a fail. it's not important if a single message gets messed up
                    }
                    return;
                }

                long myMessageId = VerifyDB.currentMyMessageId++;
                Member member = event.getMember();
                if (member == null) break;
                ColoredName coloredName = GetColoredName.get(owner);
                String username = String.format("%s [%s]",
                        coloredName.getName() == null ? member.getEffectiveName() : coloredName.getName(),
                        event.getGuild().getName());

                int color = coloredName.getColor();
                String description = event.getMessage().getContentDisplay();
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(color);
                String avatarUrl = event.getAuthor().getAvatarUrl();
                embedBuilder.setAuthor(username + " #" + myMessageId, null, avatarUrl);
                embedBuilder.setDescription(description);
                List<Message.Attachment> attachments = event.getMessage().getAttachments();
                String imageUrl = null;
                if (!attachments.isEmpty() && attachments.get(0).isImage()) {
                    imageUrl = attachments.get(0).getProxyUrl();
                }
                if (imageUrl != null) embedBuilder.setImage(imageUrl);
                MessageEmbed builtMessage = embedBuilder.build();
                List<CrossChatId> fails = new ArrayList<>(1);
                List<MessageId> messageIds = new ArrayList<>();
                for (Map.Entry<CrossChatId, MessageChannel> crossChatToSend : crossChats.entrySet()) {
                    try {
                        long sId = crossChatToSend.getKey().serverId;
                        long cId = crossChatToSend.getKey().channelId;
                        long mId = crossChatToSend.getValue().sendMessage(builtMessage).complete().getIdLong();
                        messageIds.add(new MessageId(sId, cId, mId));
                    } catch (NullPointerException | ErrorResponseException e) {
                        fails.add(crossChatToSend.getKey());
                    }
                }
                try {
                    InsertDB.insertCrossChatMessage(myMessageId, messageIds, owner, username, color, avatarUrl, imageUrl, description);
                } catch (SQLException ignored) { // it's whatever if i don't have this message saved
                }
                for (CrossChatId fail : fails) {
                    crossChats.remove(fail);
                    try {
                        InsertDB.removeCrossChat(fail.serverId);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace(); // log this
                    }
                }
                event.getMessage().delete().queue();
                lastCrossChat = new CrossChatMessage(messageIds, myMessageId, owner, username, color, avatarUrl, imageUrl, description, "");
                lastCrossChatTime = System.currentTimeMillis();
                break;
            }
        }
    }

    public static void dealWithReaction(MessageReactionAddEvent event) {
        long serverId = event.getGuild().getIdLong();
        long channelId = event.getChannel().getIdLong();
        for (CrossChatId crossChat : crossChats.keySet()) {
            if (crossChat.channelId == channelId && crossChat.serverId == serverId) {
                // we found the matched crossChat
                CrossChatMessage messagesToAddReaction;
                try {
                    messagesToAddReaction = GetDB.dealWithReactionAndGet(serverId, channelId, event.getMessageIdLong(), event);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    return;
                }
                if (messagesToAddReaction == null) return;
                if (lastCrossChat != null && lastCrossChat.myMessageId == messagesToAddReaction.myMessageId) {
                    lastCrossChat.reactions = messagesToAddReaction.reactions;
                }
                User user = event.getUser();
                if (user == null) return;
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(messagesToAddReaction.color);
                embedBuilder.setAuthor(messagesToAddReaction.username + " #" + messagesToAddReaction.myMessageId, null, messagesToAddReaction.avatarUrl);
                embedBuilder.setDescription(makeReactionMessage(messagesToAddReaction.description, messagesToAddReaction.reactions));
                if (messagesToAddReaction.imageUrl != null && !messagesToAddReaction.imageUrl.equals("null")) {
                    System.out.println(messagesToAddReaction.imageUrl);
                    embedBuilder.setImage(messagesToAddReaction.imageUrl);
                }
                for (MessageId messageId : messagesToAddReaction.messageIds) {
                    TextChannel channel = DiscordBot.client.getTextChannelById(messageId.channelId);
                    if (channel == null) continue;
                    channel.retrieveMessageById(messageId.messageId).queue(message -> {
                        if (message == null) return;
                        message.editMessage(embedBuilder.build()).queue();
                        List<MessageReaction> reactions = message.getReactions();
                        if (reactions.size() >= 15) return;
                        boolean alreadyHas = false;
                        for (MessageReaction reaction : reactions) {
                            if (equalsReactions(event.getReactionEmote(), reaction.getReactionEmote())) {
                                alreadyHas = true;
                                break;
                            }
                        }
                        if (!alreadyHas) {
                            if (event.getReactionEmote().isEmoji())
                                message.addReaction(event.getReactionEmote().getEmoji()).queue();
                            else
                                message.addReaction(event.getReactionEmote().getEmote()).queue();
                        }
                    }, failure -> { // the message doesn't exist, so just ignore that
                    });
                }
            }
        }

    }

    private static boolean equalsReactions(MessageReaction.ReactionEmote e1, MessageReaction.ReactionEmote e2) {
        return e1.isEmoji() && e2.isEmoji() ?
                e1.getEmoji().equals(e2.getEmoji())
                : e1.getEmote().getIdLong() == e2.getEmote().getIdLong();
    }

    private static String makeReactionMessage(String description, String reactions) {
        Map<String, Set<String>> reactionToUser = new HashMap<>();
        String[] reactionsSplit = reactions.split(",");
        for (String reaction : reactionsSplit) {
            String[] userToReaction = reaction.split("\\.");
            if (userToReaction.length == 2) {
                reactionToUser.putIfAbsent(userToReaction[1], new HashSet<>(8));
                reactionToUser.get(userToReaction[1]).add(userToReaction[0]);
            }
        }
        List<String> notes = new ArrayList<>();
        for (Map.Entry<String, Set<String>> reaction : reactionToUser.entrySet()) {
            notes.add(makeReactionSnippet(reaction));
        }
        StringBuilder text = new StringBuilder(description);
        for (String note : notes) {
            text.append('\n');
            if (text.length() + note.length() > 1900) {
                text.append("...and more reactions...");
            } else {
                text.append(note);
            }
        }
        return text.toString();
    }

    private static String makeReactionSnippet(Map.Entry<String, Set<String>> next) {
        return String.format("%s reacted %s", String.join(", and ", next.getValue()), next.getKey());
    }

    public static void checkIsLastMessage(List<MessageId> messages) {
        synchronized (sync) {
            if (lastCrossChat.messageIds.stream().anyMatch(messages::contains)) {
                lastCrossChat = null;
            }
        }
    }
}
