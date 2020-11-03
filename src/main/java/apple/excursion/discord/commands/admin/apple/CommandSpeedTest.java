package apple.excursion.discord.commands.admin.apple;

import apple.excursion.database.queries.GetCalendarDB;
import apple.excursion.database.queries.GetDB;
import apple.excursion.discord.commands.DoCommand;
import apple.excursion.discord.reactions.AllReactables;
import apple.excursion.sheets.SheetsTasks;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.Calendar;

public class CommandSpeedTest implements DoCommand {
    @Override
    public void dealWithCommand(MessageReceivedEvent event) {
        event.getMessage().addReaction(AllReactables.Reactable.WORKING.getFirstEmoji()).queue();
        long startGetTasks = 0, startGetCalendar = 0, startGetPlayerData = 0, startGetGuildLeaderboard = 0, startGetPlayersInGuild = 0,
                startGetGuildNameList = 0, startGetGuildSubmissions = 0, startGetPlayerLeaderboard = 0, startGetPlayerHeaders = 0;
        long endGetTasks = 0, endGetCalendar = 0, endGetPlayerData = 0, endGetGuildLeaderboard = 0, endGetPlayersInGuild = 0,
                endGetGuildNameList = 0, endGetGuildSubmissions = 0, endGetPlayerLeaderboard = 0, endGetPlayerHeaders = 0;
        try {
            startGetTasks = System.currentTimeMillis();
            SheetsTasks.getTasks();
            endGetTasks = startGetCalendar = System.currentTimeMillis();
            GetCalendarDB.getTasksToday(Calendar.getInstance());
            endGetCalendar = startGetPlayerData = System.currentTimeMillis();
            GetDB.getPlayerDataTesting(301200493307494400L); // newracket
            endGetPlayerData = startGetGuildLeaderboard = System.currentTimeMillis();
            GetDB.getGuildLeaderboard();
            endGetGuildLeaderboard = startGetPlayersInGuild = System.currentTimeMillis();
            GetDB.getPlayersInGuild("Yin", -1, -1);
            endGetPlayersInGuild = startGetGuildNameList = System.currentTimeMillis();
            GetDB.getGuildNameList();
            endGetGuildNameList = startGetGuildSubmissions = System.currentTimeMillis();
            GetDB.getGuildSubmissions("Yin", -1, -1);
            endGetGuildSubmissions = startGetPlayerLeaderboard = System.currentTimeMillis();
            GetDB.getPlayerLeaderboard();
            endGetPlayerLeaderboard = startGetPlayerHeaders = System.currentTimeMillis();
            GetDB.getPlayerHeaders();
            endGetPlayerHeaders = System.currentTimeMillis();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            event.getChannel().sendMessage("SQLException").queue();
        }
        StringBuilder text = new StringBuilder();
        text.append(String.format("GetTasks took %d millis\n", endGetTasks - startGetTasks));
        text.append(String.format("GetCalendar took %d millis\n", endGetCalendar - startGetCalendar));
        text.append(String.format("GetPlayerData took %d millis\n", endGetPlayerData - startGetPlayerData));
        text.append(String.format("getGuildLeaderboard took %d millis\n", endGetGuildLeaderboard - startGetGuildLeaderboard));
        text.append(String.format("getPlayersInGuild took %d millis\n", endGetPlayersInGuild - startGetPlayersInGuild));
        text.append(String.format("getGuildNameList took %d millis\n", endGetGuildNameList - startGetGuildNameList));
        text.append(String.format("getGuildSubmission took %d millis\n", endGetGuildSubmissions - startGetGuildSubmissions));
        text.append(String.format("GetPlayerLeaderboard took %d millis\n", endGetPlayerLeaderboard - startGetPlayerLeaderboard));
        text.append(String.format("getPlayerHeaders took %d millis\n", endGetPlayerHeaders - startGetPlayerHeaders));
        event.getChannel().sendMessage(text.toString()).queue();
    }
}
