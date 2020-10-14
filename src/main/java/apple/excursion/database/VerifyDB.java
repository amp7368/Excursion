package apple.excursion.database;

import apple.excursion.ExcursionMain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class VerifyDB {
    private static final String DATABASE_FOLDER;
    private static final String SUBMISSION_DB;
    private static final String PLAYER_DB;
    private static final String GUILD_LB_DB;
    private static final String PLAYER_LB_DB;
    public static Connection submissionDbConnection;
    public static Connection playerDbConnection;
    public static Connection guildLbDbConnection;
    public static Connection playerLbDbConnection;

    public static final Object syncDB = new Object();
    public static int currentSubmissionId;

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        DATABASE_FOLDER = String.join("/", list.subList(0, list.size() - 1)) + "/data/";
        PLAYER_LB_DB = DATABASE_FOLDER + "playerlb.db";
        GUILD_LB_DB = DATABASE_FOLDER + "guildlb.db";
        PLAYER_DB = DATABASE_FOLDER + "player.db";
        SUBMISSION_DB = DATABASE_FOLDER + "submissions.db";
    }


    public static void verify() throws ClassNotFoundException, SQLException {
        synchronized (syncDB) {
            Class.forName("org.sqlite.JDBC");
            submissionDbConnection = DriverManager.getConnection("jdbc:sqlite:" + SUBMISSION_DB);
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
                            + "	submission_ids TEXT NOT NULL"
                            + ");";
            statement = playerDbConnection.createStatement();
            statement.execute(buildTableSql);
            statement.close();


            guildLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + GUILD_LB_DB);
            buildTableSql =
                    "CREATE TABLE IF NOT EXISTS " + "OCT_2020" + " ("
                            + "	guild_tag TEXT NOT NULL,"
                            + "	guild_name TEXT NOT NULL,"
                            + "	score INTEGER NOT NULL,"
                            + "	submissions_count INTEGER NOT NULL,"
                            + "PRIMARY KEY(guild_tag,guild_name)"
                            + ");";
            statement = guildLbDbConnection.createStatement();
            statement.execute(buildTableSql);
            statement.close();


            playerLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + PLAYER_LB_DB);
            buildTableSql =
                    "CREATE TABLE IF NOT EXISTS " + "OCT_2020" + "("
                            + "	player_uid INTEGER PRIMARY KEY NOT NULL UNIQUE,"
                            + "	score INTEGER NOT NULL,"
                            + "	submissions_count INTEGER NOT NULL"
                            + ");";
            statement = playerLbDbConnection.createStatement();
            statement.execute(buildTableSql);
            statement.close();
        }
    }
}
