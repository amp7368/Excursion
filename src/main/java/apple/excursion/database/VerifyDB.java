package apple.excursion.database;

import apple.excursion.ExcursionMain;
import apple.excursion.discord.data.Task;
import apple.excursion.sheets.SheetsTasks;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.Date;

public class VerifyDB {
    private static final String DATABASE_FOLDER;
    private static final String SUBMISSION_DB;
    private static final String PLAYER_DB;
    private static final String GUILD_LB_DB;
    private static final String PLAYER_LB_DB;
    private static final String GUILD_DB;
    private static final String CALENDAR_DB;
    private static final int TASKS_PER_DAY = 3;
    public static Connection submissionDbConnection;
    public static Connection playerDbConnection;
    public static Connection guildLbDbConnection;
    public static Connection playerLbDbConnection;
    public static Connection guildDbConnection;
    public static Connection calendarDbConnection;

    public static final Object syncDB = new Object();
    public static int currentSubmissionId;

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        DATABASE_FOLDER = String.join("/", list.subList(0, list.size() - 1)) + "/data/";
        PLAYER_LB_DB = DATABASE_FOLDER + "playerlb.db";
        GUILD_LB_DB = DATABASE_FOLDER + "guildlb.db";
        PLAYER_DB = DATABASE_FOLDER + "player.db";
        SUBMISSION_DB = DATABASE_FOLDER + "submissions.db";
        GUILD_DB = DATABASE_FOLDER + "guild.db";
        CALENDAR_DB = DATABASE_FOLDER + "calendar.db";
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            submissionDbConnection = DriverManager.getConnection("jdbc:sqlite:" + SUBMISSION_DB);
            guildLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + GUILD_LB_DB);
            playerLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + PLAYER_LB_DB);
            guildDbConnection = DriverManager.getConnection("jdbc:sqlite:" + GUILD_DB);
            calendarDbConnection = DriverManager.getConnection("jdbc:sqlite:" + CALENDAR_DB);
            verify();
            verifyCalendar();
        }
    }

    /**
     * this should always be within a synchronized
     *
     * @throws SQLException
     */
    public static void verify() throws SQLException {
        String month = getMonth();
        String buildTableSql =
                "CREATE TABLE IF NOT EXISTS submissions ("
                        + "	id INTEGER PRIMARY KEY NOT NULL UNIQUE,"
                        + "	date_submitted TEXT NOT NULL,"
                        + "	task_name TEXT NOT NULL,"
                        + "	links TEXT,"
                        + "	submitter TEXT NOT NULL,"
                        + "	all_submitters TEXT"
                        + ");";
        Statement statement = submissionDbConnection.createStatement();
        statement.execute(buildTableSql);
        currentSubmissionId = statement.executeQuery("SELECT MAX(id) FROM submissions;").getInt(1) + 1;
        statement.close();
        playerDbConnection = DriverManager.getConnection("jdbc:sqlite:" + PLAYER_DB);
        buildTableSql =
                "CREATE TABLE IF NOT EXISTS players ("
                        + "	player_uid TEXT PRIMARY KEY NOT NULL UNIQUE,"
                        + "	player_name TEXT NOT NULL,"
                        + "	guild_name TEXT,"
                        + "	guild_tag TEXT,"
                        + "	submission_ids TEXT NOT NULL,"
                        + "	score SCORE NOT NULL"
                        + ");";
        statement = playerDbConnection.createStatement();
        statement.execute(buildTableSql);
        statement.close();

        buildTableSql =
                "CREATE TABLE IF NOT EXISTS " + month + " ("
                        + "	guild_tag TEXT NOT NULL PRIMARY KEY UNIQUE,"
                        + "	guild_name TEXT NOT NULL,"
                        + "	score INTEGER NOT NULL,"
                        + "	submissions_count INTEGER NOT NULL"
                        + ");";
        statement = guildLbDbConnection.createStatement();
        statement.execute(buildTableSql);
        statement.close();


        buildTableSql =
                "CREATE TABLE IF NOT EXISTS " + month + " ("
                        + "	player_uid INTEGER PRIMARY KEY NOT NULL UNIQUE,"
                        + "	score INTEGER NOT NULL,"
                        + "	submissions_count INTEGER NOT NULL"
                        + ");";
        statement = playerLbDbConnection.createStatement();
        statement.execute(buildTableSql);
        statement.close();


        buildTableSql =
                "CREATE TABLE IF NOT EXISTS guilds ("
                        + "	guild_tag TEXT PRIMARY KEY NOT NULL UNIQUE,"
                        + "	guild_name TEXT NOT NULL, "
                        + "	submissions TEXT NOT NULL"
                        + ");";
        statement = guildDbConnection.createStatement();
        statement.execute(buildTableSql);
        statement.close();

        buildTableSql =
                "CREATE TABLE IF NOT EXISTS calendar ("
                        + "	month_name TEXT PRIMARY KEY NOT NULL UNIQUE "
                        + ");";
        statement = calendarDbConnection.createStatement();
        statement.execute(buildTableSql);
        statement.close();
    }

    /**
     * this should always be within a synchronized
     *
     * @throws SQLException
     */
    public static void verifyCalendar() throws SQLException {
        String monthName = getMonth();

        String sql = String.format("SELECT COUNT(1) " +
                        "FROM calendar " +
                        "WHERE month_name = '%s' " +
                        "LIMIT 1;",
                monthName);
        System.out.println(sql);
        Statement statement = calendarDbConnection.createStatement();
        ResultSet response = statement.executeQuery(sql);
        final boolean exists = response.getInt(1) == 1;
        response.close();
        if (!exists) {
            // make a new calendar
            sql = "CREATE TABLE IF NOT EXISTS " + monthName + " ( " +
                    "date INTEGER PRIMARY KEY NOT NULL UNIQUE," +
                    "task_names TEXT NOT NULL );";
            statement.execute(sql);

            // record the calendar db name
            sql = "INSERT INTO calendar(month_name) "
                    + "VALUES "
                    + String.format("('%s');",
                    monthName
            );
            statement.execute(sql);

            List<Task> tasks = SheetsTasks.getTasks();
            int size = tasks.size();
            Random random = new Random();
            List<Collection<Task>> dailyTasks = new ArrayList<>();
            // fill in calendar
            int daysThisMonth = YearMonth.now().lengthOfMonth();
            int randomNext;
            for (int i = 0; i < daysThisMonth; i++) {
                Collection<Task> todayTasks = new ArrayList<>();
                Collection<Integer> todayNumbers = new ArrayList<>();
                for (int j = 0; j < TASKS_PER_DAY; j++) {
                    //noinspection StatementWithEmptyBody
                    while (todayNumbers.contains(randomNext = random.nextInt())) ;
                    todayNumbers.add(randomNext);
                    todayTasks.add(tasks.get(random.nextInt(size)));
                }
                dailyTasks.add(todayTasks);
            }
            for (int i = 0; i < daysThisMonth; i++) {
                Collection<Task> todayTasks = dailyTasks.get(i);
                sql = GetSql.getSqlInsertDailyTask(monthName, i + 1, todayTasks);
                statement.addBatch(sql);
            }
            statement.executeBatch();
        }
        statement.close();
    }

    public static void verifyCalendar(String monthName,int daysThisMonth) throws SQLException {
        Statement statement = calendarDbConnection.createStatement();
        // make a new calendar
        String sql = "CREATE TABLE IF NOT EXISTS " + monthName + " ( " +
                "date INTEGER PRIMARY KEY NOT NULL UNIQUE," +
                "task_names TEXT NOT NULL );";
        statement.execute(sql);

        // record the calendar db name
        sql = "INSERT INTO calendar(month_name) "
                + "VALUES "
                + String.format("('%s');",
                monthName
        );
        statement.execute(sql);

        List<Task> tasks = SheetsTasks.getTasks();
        int size = tasks.size();
        Random random = new Random();
        List<Collection<Task>> dailyTasks = new ArrayList<>();
        // fill in calendar
        int randomNext;
        for (int i = 0; i < daysThisMonth; i++) {
            Collection<Task> todayTasks = new ArrayList<>();
            Collection<Integer> todayNumbers = new ArrayList<>();
            for (int j = 0; j < TASKS_PER_DAY; j++) {
                //noinspection StatementWithEmptyBody
                while (todayNumbers.contains(randomNext = random.nextInt())) ;
                todayNumbers.add(randomNext);
                todayTasks.add(tasks.get(random.nextInt(size)));
            }
            dailyTasks.add(todayTasks);
        }
        for (int i = 0; i < daysThisMonth; i++) {
            Collection<Task> todayTasks = dailyTasks.get(i);
            sql = GetSql.getSqlInsertDailyTask(monthName, i + 1, todayTasks);
            statement.addBatch(sql);
        }
        statement.executeBatch();
    }

    private static String getMonth() {
        return getMonthFromDate(System.currentTimeMillis());
    }

    public static String getMonthFromDate(Long epochMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("MMM'_'yyyy");
        return formatter.format(new Date(epochMilliseconds)).toUpperCase();
    }


}
