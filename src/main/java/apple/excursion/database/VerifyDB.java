package apple.excursion.database;

import apple.excursion.ExcursionMain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class VerifyDB {
    private static final String SUBMISSION_DB;
    private static final String PLAYER_DB;
    private static final String GUILD_LB_DB;
    private static final String PLAYER_LB_DB;
    private static Connection submissionDbConnection;
    private static Connection playerDbConnection;
    private static Connection guildLbDbConnection;
    private static Connection playerLbDbConnection;
    private static final String DATABASE_FOLDER;

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        DATABASE_FOLDER = String.join("/", list.subList(0, list.size() - 1)) + "/data/";
        PLAYER_LB_DB = DATABASE_FOLDER + "playerlb.db";
        GUILD_LB_DB = DATABASE_FOLDER + "guildlb.db";
        PLAYER_DB = DATABASE_FOLDER + "player.db";
        SUBMISSION_DB = DATABASE_FOLDER + "submissions.db";
    }

    public static void verify() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        submissionDbConnection = DriverManager.getConnection("jdbc:sqlite:" + SUBMISSION_DB);
        String buildTableSql =
                "CREATE TABLE IF NOT EXISTS submissions ("
                        + "	id INTEGER PRIMARY KEY NOT NULL UNIQUE,"
                        + "	date_submitted DATETIME NOT NULL,"
                        + "	acceptor TEXT NOT NULL,"
                        + "	task_name TEXT NOT NULL,"
                        + "	links TEXT,"
                        + "	submitter TEXT NOT NULL,"
                        + "	all_submitters TEXT"
                        + ");";
        submissionDbConnection.createStatement().execute(buildTableSql);

        playerDbConnection = DriverManager.getConnection("jdbc:sqlite:" + PLAYER_DB);
        buildTableSql =
                "CREATE TABLE IF NOT EXISTS players ("
                        + "	player_uid TEXT PRIMARY KEY NOT NULL UNIQUE,"
                        + "	player_name TEXT NOT NULL,"
                        + "	submission_ids TEXT"
                        + ");";
        playerDbConnection.createStatement().execute(buildTableSql);


        guildLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + GUILD_LB_DB);
        buildTableSql =
                "CREATE TABLE IF NOT EXISTS " + "OCT_2020" + " ("
                        + "	guild_tag TEXT NOT NULL,"
                        + "	guild_name TEXT NOT NULL,"
                        + "	score INTEGER NOT NULL,"
                        + "	submissions_count INTEGER NOT NULL,"
                        + "PRIMARY KEY(guild_tag,guild_name)"
                        + ");";
        guildLbDbConnection.createStatement().execute(buildTableSql);


        playerLbDbConnection = DriverManager.getConnection("jdbc:sqlite:" + PLAYER_LB_DB);
        buildTableSql =
                "CREATE TABLE IF NOT EXISTS " + "OCT_2020" + "("
                        + "	player_uid INTEGER PRIMARY KEY NOT NULL UNIQUE,"
                        + "	score INTEGER NOT NULL,"
                        + "	submissions_count INTEGER NOT NULL"
                        + ");";
        playerLbDbConnection.createStatement().execute(buildTableSql);
    }
}
