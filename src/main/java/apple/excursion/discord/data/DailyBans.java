package apple.excursion.discord.data;

import apple.excursion.ExcursionMain;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;
import java.util.List;

public class DailyBans {
    private static final String BANS_FILE_PATH;
    private static final Set<String> bans = new HashSet<>();
    private static final Object sync = new Object();

    static {
        List<String> list = Arrays.asList(ExcursionMain.class.getProtectionDomain().getCodeSource().getLocation().getPath().split("/"));
        BANS_FILE_PATH = String.join("/", list.subList(0, list.size() - 1)) + "/data/dailyBans.json";
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(BANS_FILE_PATH)));
            JSONParser parser = new JSONParser();
            Object result = parser.parse(in);
            JSONArray reviewersArray = (JSONArray) result;
            for (Object entry : reviewersArray) {
                bans.add(entry.toString());
            }
            in.close();
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static boolean isBan(String daily) {
        synchronized (sync) {
            return bans.contains(daily);
        }
    }

    public static void addBan(String ban) throws IOException {
        synchronized (sync) {
            bans.add(ban);
            writeReviewers();
        }
    }

    public static void removeBan(String ban) throws IOException {
        synchronized (sync) {
            bans.remove(ban);
            writeReviewers();
        }
    }

    @SuppressWarnings("unchecked")
    private static void writeReviewers() throws IOException {
        JSONArray reviewersArray = new JSONArray();
        reviewersArray.addAll(bans);
        File file = new File(BANS_FILE_PATH);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Could not create new file: " + BANS_FILE_PATH);
            }
        }
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file));
        out.write(reviewersArray.toJSONString());
        out.flush();
        out.close();
    }

    public static List<String> getBans() {
        synchronized (sync) {
            return new ArrayList<>(bans);
        }
    }
}
