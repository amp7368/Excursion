package apple.excursion.discord.commands.general.benchmark;

import apple.excursion.database.objects.OldSubmission;
import apple.excursion.database.objects.player.PlayerData;
import apple.excursion.database.objects.player.PlayerHeader;
import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.data.Task;
import apple.excursion.discord.reactions.messages.benchmark.CompletedTasksMessage;
import apple.excursion.sheets.SheetsTasks;
import apple.excursion.utils.ColoredName;
import apple.excursion.utils.GetColoredName;
import apple.excursion.utils.Pair;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandCompleted implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        String[] content = event.getMessage().getContentStripped().split(" ", 2);
        ColoredName coloredName = GetColoredName.get(event.getAuthor().getIdLong());
        PlayerData player;
        if (content.length > 1) {
            String nameToGet = content[1];
            List<PlayerHeader> playerHeaders;
            try {
                playerHeaders = GetDB.getPlayerHeaders();
            } catch (SQLException throwables) {
                event.getChannel().sendMessage("There was an SQLException getting player names").queue();
                return;
            }
            List<PlayerHeader> playersWithName = new ArrayList<>();
            Pattern pattern = Pattern.compile(".*" + nameToGet + ".*", Pattern.CASE_INSENSITIVE);
            for (PlayerHeader header : playerHeaders) {
                if (pattern.matcher(header.name).matches()) {
                    playersWithName.add(header);
                }
            }
            if (playersWithName.isEmpty()) {
                event.getChannel().sendMessage(String.format("There are no players that contain'%s'", nameToGet)).queue();
                return;
            } else if (playersWithName.size() > 1) {
                event.getChannel().sendMessage(String.format("There are %d players that contain '%s'", playersWithName.size(), nameToGet)).queue();
                return;
            } else {
                try {
                    player = GetDB.getPlayerData(new Pair<>(playersWithName.get(0).id, playersWithName.get(0).name));
                } catch (SQLException throwables) {
                    event.getChannel().sendMessage("There was an SQLException getting the playerData for " + playersWithName.get(0).name).queue();
                    return;
                }
            }
        } else {
            Member member = event.getMember();
            if (member == null) return;// the member should always exist
            String name = coloredName.getName() == null ? ColoredName.getGuestName(member.getEffectiveName()) : coloredName.getName();
            try {
                player = GetDB.getPlayerData(new Pair<>(member.getIdLong(), name));
            } catch (SQLException throwables) {
                event.getChannel().sendMessage("There was an SQLException getting your playerdata").queue();
                return;
            }
        }
        Map<Task, List<OldSubmission>> taskNameToSubmissions = new HashMap<>();
        List<Task> tasksList = SheetsTasks.getTasks();
        // make the info for anything that contains that quest
        for (Task task : tasksList) {
            taskNameToSubmissions.put(task, new ArrayList<>());
        }
        // fill out the map with submissions
        for (OldSubmission submission : player.submissions) {
            Pattern pattern = Pattern.compile(".*" + submission.taskName + ".*", Pattern.CASE_INSENSITIVE);
            for (Task task : taskNameToSubmissions.keySet()) {
                if (pattern.matcher(task.name).matches()) {
                    taskNameToSubmissions.get(task).add(submission);
                    break; // go to next submission
                }
            }
        }
        new CompletedTasksMessage(player,
                taskNameToSubmissions.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList()),
                event.getChannel());
    }
}
