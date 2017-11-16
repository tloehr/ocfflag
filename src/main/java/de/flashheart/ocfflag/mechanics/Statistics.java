package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

public class Statistics {

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
       static String host = "wp12617924.server-he.de";
       static String user = "ftp12617924-923";
       static String pwd = "";

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
//
//    private void ftpUpload(){
//        File tempHTMLFile = File.createTempFile("ocfflag","html");
//              tempHTMLFile.deleteOnExit();
//              FileUtils.writeStringToFile(tempHTMLFile, exampleString);
//              FtpUploadDownloadUtil.upload(tempHTMLFile.getAbsolutePath(), uuid.toString()+".html", host, 21, user, pwd, true);
//
//              File tempMatchIDFile = File.createTempFile("ocfflag","txt");
//              tempMatchIDFile.deleteOnExit();
//
//              // Gibts eine Matchid auf dem FTP Server ?
//              if (!FtpUploadDownloadUtil.download(tempMatchIDFile.getAbsolutePath(), uuid.toString()+".txt", host, 21, user, pwd, true)){
//                  // wenn nicht, dann legen wir jetzt eine an.
//                  FileUtils.writeStringToFile(tempMatchIDFile, Integer.toString(matchid));
//                  FtpUploadDownloadUtil.upload(tempMatchIDFile.getAbsolutePath(), uuid.toString()+".txt", host, 21, user, pwd, true);
//              } else {
//                  FileUtils.writeStringToFile(tempMatchIDFile, Integer.toString(matchid));
//                  int remoteMatchID = Integer.parseInt(FileUtils.readFileToString(tempHTMLFile));
//                  if (remoteMatchID != matchid){
//                      FtpUploadDownloadUtil.upload(tempMatchIDFile.getAbsolutePath(), uuid.toString()+".txt", host, 21, user, pwd, true);
//                  }
//              }
//
//              System.out.println("Done");
//    }

    public void reset() {
        if (!stackEvents.isEmpty()) {
            logger.debug("CLOSE AND SEND Statistics list");
        }
        endOfGame = null;
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
     *
     * @return true, wenn die Operation erfolgreich war.
     */
    public boolean sendStats() {

        logger.debug(toPHP());
        return true;
    }

    public boolean addEvent(int event) {
        if (min_stat_sent_time == 0) return false;
        logger.debug(EVENTS[event]);
        DateTime now = new DateTime();
        stackEvents.push(new GameEvent(now, event));

        if (event == EVENT_GAME_ABORTED || event == EVENT_GAME_OVER){
            endOfGame = now;
        } else {
            endOfGame = null;
        }

        if (event == EVENT_RESULT_RED_WON) winningTeam = "red";
        if (event == EVENT_RESULT_BLUE_WON) winningTeam = "blue";

        sendStats(); // jedes Ereignis wird gesendet.
        return true;
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
        php += "$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n";
        php += "$game['matchid'] = '" + matchid + "';\n";
        php += "$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n";
        php += "$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.get(0).getPit()) + "';\n";
        php += "$game['ts_game_ended'] = '" + endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame) + "';\n";
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
