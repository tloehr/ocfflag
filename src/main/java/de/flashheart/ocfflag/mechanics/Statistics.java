package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.FtpUploadDownloadUtil;
import de.flashheart.ocfflag.misc.Observable;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Stack;

public class Statistics extends Observable<Boolean> {

    private long min_stat_sent_time = 0l;
    private long time, time_blue, time_red;

    private final Logger logger = Logger.getLogger(getClass());


    public static final int EVENT_PAUSE = 0;
    public static final int EVENT_RESUME = 1;
    public static final int EVENT_START_GAME = 2; // von Standby nach Active
    public static final int EVENT_BLUE_ACTIVATED = 3;
    public static final int EVENT_RED_ACTIVATED = 4;
    public static final int EVENT_GAME_OVER = 5; // wenn die Spielzeit abgelaufen ist
    public static final int EVENT_GAME_ABORTED = 6; // wenn die Spielzeit abgelaufen ist
    public static final int EVENT_RESULT_RED_WON = 7; // wenn das spiel vorzeitig beendet wird
    public static final int EVENT_RESULT_BLUE_WON = 8; // wenn das spiel vorzeitig beendet wird
    public static final int EVENT_RESULT_DRAW = 9; // wenn das spiel vorzeitig beendet wird


    public static final String[] EVENTS = new String[]{"EVENT_PAUSE", "EVENT_RESUME", "EVENT_START_GAME",
            "EVENT_BLUE_ACTIVATED", "EVENT_RED_ACTIVATED", "EVENT_GAME_OVER", "EVENT_GAME_ABORTED",
            "EVENT_RESULT_RED_WON", "EVENT_RESULT_BLUE_WON", "EVENT_RESULT_DRAW"};

    public Stack<GameEvent> stackEvents;
    private int matchid;
    private DateTime endOfGame = null;
    private String winningTeam = "";

    static FTPClient ftp = null;
    private String flagcolor;

    public static void uploadFile(String localFileFullName, String fileName, String hostDir)
            throws Exception {
        try (InputStream input = new FileInputStream(new File(localFileFullName))) {
            ftp.storeFile(hostDir + fileName, input);
        }
    }

    public Statistics() {
        logger.setLevel(Main.getLogLevel());
        stackEvents = new Stack<>();
        min_stat_sent_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        reset();
    }

    private boolean ftpUpload() throws IOException {
        if (!Main.getConfigs().isFTPComplete()) return false;

        File tempPHPFile = File.createTempFile("ocfflag", "php");
        tempPHPFile.deleteOnExit();
        FileUtils.writeStringToFile(tempPHPFile, toPHP(), "UTF-8");

        String remoteFile = stackEvents.get(0).getPit().getMillis()+"-" + Main.getConfigs().get(Configs.MYUUID) + ".php";

        boolean success = FtpUploadDownloadUtil.upload(tempPHPFile.getAbsolutePath(), Main.getConfigs().get(Configs.FTPREMOTEPATH) + "/", remoteFile,
                Main.getConfigs().get(Configs.FTPHOST),
                Integer.parseInt(Main.getConfigs().get(Configs.FTPPORT)),
                Main.getConfigs().get(Configs.FTPUSER),
                Main.getConfigs().get(Configs.FTPPWD), true);

        return success;
    }

    public void moveActiveToArchive(){

    }

    public void reset() {
        if (!stackEvents.isEmpty()) {
            logger.debug("CLOSE AND SEND Statistics list");
            // move active file to archive on FTP Server
        }
        endOfGame = null;
        flagcolor = "neutral";
        winningTeam = "not_yet";
        time = 0l;
        time_blue = 0l;
        time_red = 0l;
        matchid = 0;
        stackEvents.clear();
    }

    public void setTimes(int matchid, long time, long time_blue, long time_red) {
        this.matchid = matchid;
        this.time = time;
        this.time_blue = time_blue;
        this.time_red = time_red;
    }

    /**
     * @return true, wenn die Operation erfolgreich war.
     */
    public void sendStats() {
        logger.debug(toPHP());
        if (min_stat_sent_time > 0 && Main.getConfigs().isFTPComplete()) {
            SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
                boolean success = false;

                @Override
                protected Boolean doInBackground() throws Exception {
                    try {
                        success = ftpUpload();
                    } catch (IOException e) {
                        logger.info(e);
                        success = false;
                    }

                    return success;
                }

                @Override
                protected void done() {
                    try {
                        notifyObservers(get());
                    } catch (Exception e) {
                        logger.debug(e);
                    }
                }
            };
            worker.execute();
        }
    }

    public void addEvent(int event) {
        if (min_stat_sent_time == 0) return;
        logger.debug(EVENTS[event]);
        DateTime now = new DateTime();
        stackEvents.push(new GameEvent(now, event));

        if (event == EVENT_GAME_ABORTED || event == EVENT_GAME_OVER) {
            endOfGame = now;
        } else {
            endOfGame = null;
        }

        if (event == EVENT_RESULT_RED_WON) winningTeam = "red";
        if (event == EVENT_RESULT_BLUE_WON) winningTeam = "blue";
        if (event == EVENT_RED_ACTIVATED) flagcolor = "red";
        if (event == EVENT_BLUE_ACTIVATED) flagcolor = "blue";

        sendStats(); // jedes Ereignis wird gesendet.
    }

    private class GameEvent {
        private DateTime pit;
        private int event;

        public GameEvent(DateTime pit, int event) {
            this.pit = pit;
            this.event = event;
        }

        public int getEvent() {
            return event;
        }

        public DateTime getPit() {
            return pit;
        }

        @Override
        public String toString() {
            return "GameEvent{" +
                    "pit=" + pit.toString(DateTimeFormat.mediumDateTime()) +
                    ", event=" + EVENTS[event] +
                    '}';
        }

        public String toPHPArray() {
            return "   array('pit' => '" + pit.toString("HH:mm:ss") + "','event' => '" + EVENTS[event] + "'),\n";
        }
    }

    private String toPHP() {
        String php = "<?php\n";

        php += "$game['flagname'] = '" + Main.getConfigs().get(Configs.FLAGNAME) + "';\n";
        php += "$game['flagcolor'] = '" + flagcolor + "';\n";
        php += "$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n";
        php += "$game['matchid'] = '" + matchid + "';\n";
        php += "$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n";
        php += "$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.get(0).getPit()) + "';\n";
        php += "$game['ts_game_ended'] = '" + (endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame)) + "';\n";
        php += "$game['winning_team'] = '" + winningTeam + "';\n";
        php += "$game['time'] = '" + Tools.formatLongTime(time, "HH:mm:ss") + "';\n";
        php += "$game['time_blue'] = '" + Tools.formatLongTime(time_blue, "HH:mm:ss") + "';\n";
        php += "$game['time_red'] = '" + Tools.formatLongTime(time_red, "HH:mm:ss") + "';\n";

        php += "$game['events'] = array(\n";
        for (GameEvent event : stackEvents) {
            php += event.toPHPArray();
        }

        php += ");\n";


        return php + "?>";
    }


}
