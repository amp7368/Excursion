package apple.excursion.database;

import apple.excursion.ExcursionMain;
import apple.excursion.database.queries.GetSql;
import apple.excursion.discord.data.Task;
import apple.excursion.sheets.SheetsTasks;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.*;
import java.util.Date;

public class VerifyDB {
    private static final String DATABASE_FOLDER;
    private static final String DB_DB;
    private static final String CALENDAR_DB;
    private static final int TASKS_PER_DAY = 3;
    public static final String DEFAULT_GUILD_TAG = "DEFAULT_TAG";
    public static final String DEFAULT_GUILD_NAME = "DEFAULT_NAME";
    public static final String BUILD_TABLE_SQL_SUBMISSIONS = "CREATE TABLE IF NOT EXISTS submissions ("
            + "	id INTEGER PRIMARY KEY NOT NULL UNIQUE,"
            + "	date_submitted TIMESTAMP NOT NULL,"
            + "	task_name TEXT NOT NULL,"
            + "	links TEXT,"
            + "	submitter BIGINT NOT NULL,"
            + "	submission_type TEXT NOT NULL,"
            + " score INTEGER NOT NULL DEFAULT 0, "
            + " FOREIGN KEY (submitter) REFERENCES players (player_uid)"
            + ");";
    public static final String BUILD_TABLE_SQL_PLAYERS = "CREATE TABLE IF NOT EXISTS players ("
            + "	player_uid TEXT PRIMARY KEY NOT NULL UNIQUE,"
            + "	player_name TEXT NOT NULL,"
            + "	guild_name TEXT NOT NULL DEFAULT '" + DEFAULT_GUILD_NAME + "',"
            + "	guild_tag TEXT NOT NULL DEFAULT '" + DEFAULT_GUILD_TAG + "',"
            + "	soul_juice INTEGER NOT NULL DEFAULT 0, "
            + " FOREIGN KEY (guild_tag) REFERENCES guilds (guild_tag)"
            + ");";
    public static final String BUILD_TABLE_SQL_GUILDS = "CREATE TABLE IF NOT EXISTS guilds ("
            + "	guild_tag TEXT NOT NULL PRIMARY KEY UNIQUE,"
            + "	guild_name TEXT NOT NULL"
            + ");";
    public static final String BUILD_TABLE_SQL_SUBMISSION_LINK = "CREATE TABLE IF NOT EXISTS submissions_link ( " +
            "submission_id INTEGER NOT NULL, " +
            "player_id BIG INT NOT NULL, " +
            "guild_tag TEXT NOT NULL DEFAULT '" + DEFAULT_GUILD_TAG + "'," +
            "PRIMARY KEY (submission_id, player_id), " +
            "UNIQUE (submission_id, player_id), " +
            "FOREIGN KEY (submission_id) REFERENCES submissions (id), " +
            "FOREIGN KEY (player_id) REFERENCES players (player_uid), " +
            "FOREIGN KEY (guild_tag) REFERENCES guilds (guild_tag) " +
            ");";
    private static final String INSERT_DEFAULT_GUILD = "INSERT INTO guilds VALUES ('" + DEFAULT_GUILD_TAG + "','" + DEFAULT_GUILD_NAME + "');";
    public static Connection database;
    public static Connection calendarDbConnection;

    public static final Object syncDB = new Object();
    public static int currentSubmissionId;

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        DATABASE_FOLDER = String.join("/", list.subList(0, list.size() - 1)) + "/data/";
        DB_DB = DATABASE_FOLDER + "data.db";
        CALENDAR_DB = DATABASE_FOLDER + "calendar.db";
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            database = DriverManager.getConnection("jdbc:sqlite:" + DB_DB);
            calendarDbConnection = DriverManager.getConnection("jdbc:sqlite:" + CALENDAR_DB);
            verify();
            verifyCalendar();
        }
    }

    /**
     * this should always be within a synchronized
     *
     * @throws SQLException when sql goes bad
     */
    public static void verify() throws SQLException {
        Statement statement = database.createStatement();
        String buildTableSql = BUILD_TABLE_SQL_SUBMISSIONS;
        statement.execute(buildTableSql);

        buildTableSql = BUILD_TABLE_SQL_PLAYERS;
        statement.execute(buildTableSql);

        buildTableSql = BUILD_TABLE_SQL_GUILDS;
        statement.execute(buildTableSql);

        buildTableSql = INSERT_DEFAULT_GUILD;
        try {
            statement.execute(buildTableSql);
        } catch (SQLException ignored) {
        }

        buildTableSql = BUILD_TABLE_SQL_SUBMISSION_LINK;
        statement.execute(buildTableSql);

        currentSubmissionId = statement.executeQuery("SELECT MAX(id) FROM submissions;").getInt(1) + 1;
        statement.close();

        buildTableSql = "CREATE TABLE IF NOT EXISTS calendar ("
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

    private static String getMonth() {
        return getMonthFromDate(System.currentTimeMillis());
    }

    public static String getMonthFromDate(Long epochMilliseconds) {
        SimpleDateFormat formatter = new SimpleDateFormat();
        formatter.applyPattern("MMM'_'yyyy");
        return formatter.format(new Date(epochMilliseconds)).toUpperCase();
    }


}
