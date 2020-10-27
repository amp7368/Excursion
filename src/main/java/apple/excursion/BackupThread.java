package apple.excursion;

import apple.excursion.utils.SendLogs;

public class BackupThread extends Thread {
    private static final long DAY = 24 * 60 * 60 * 1000;

    @Override
    public void run() {
        while (true) {
            try {
                SendLogs.sendDbBackup();
            } catch (Exception e) { // catch everything. This is important that it keeps running
                e.printStackTrace();
            }
            try {
                Thread.sleep(DAY);
            } catch (InterruptedException ignored) { // catch everything. This is important that it keeps running
            }
        }
    }
}
