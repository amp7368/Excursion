package apple.excursion;

import apple.excursion.utils.SendLogs;

public class BackupThread extends Thread {
    private static final long WEEK = 7 * 24 * 60 * 60 * 1000;

    @Override
    public void run() {
        while (true) {
            SendLogs.sendDbBackup();
            try {
                Thread.sleep(WEEK);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
