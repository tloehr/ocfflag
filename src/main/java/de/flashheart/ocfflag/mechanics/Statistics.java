package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.PHPMessage;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;
import java.util.Locale;
import java.util.Stack;

public class Statistics {

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
    private SwingWorker<Boolean, Boolean> worker;

    private String flagcolor;

    public Statistics() {
        logger.setLevel(Main.getLogLevel());
        stackEvents = new Stack<>();
        reset();
    }

    public void reset() {
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

    public void sendStats() {
        logger.debug(toPHP());
        if (Main.getMessageProcessor() != null)
            Main.getMessageProcessor().pushMessage(new PHPMessage(toPHP(), stackEvents.peek()));
    }

    public long addEvent(int event) {
        DateTime now = new DateTime();
//        if (Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME)) > 0) return now.getMillis();

        stackEvents.push(new GameEvent(now, event));

        if (endOfGame == null) {
            if (event == EVENT_GAME_ABORTED || event == EVENT_GAME_OVER) {
                endOfGame = now;
            }
        }

        if (event == EVENT_RESULT_RED_WON) winningTeam = "red";
        if (event == EVENT_RESULT_BLUE_WON) winningTeam = "blue";
        if (event == EVENT_RESULT_DRAW) winningTeam = "draw";
        if (event == EVENT_RED_ACTIVATED) flagcolor = "red";
        if (event == EVENT_BLUE_ACTIVATED) flagcolor = "blue";

        sendStats(); // jedes Ereignis wird gesendet.

        return now.getMillis();
    }



    private String toPHP() {
        String php = "<?php\n";

        String flagname = Main.getConfigs().get(Configs.FLAGNAME);

        flagname = StringUtils.replace(flagname, "'", "\\'");
        flagname = StringUtils.replace(flagname, "\"", "\\\"");


        php += "$game['flagname'] = '" + flagname + "';\n";
        php += "$game['flagcolor'] = '" + flagcolor + "';\n";
        php += "$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n";
        php += "$game['matchid'] = '" + matchid + "';\n";
        php += "$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n";
        php += "$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.get(0).getPit()) + "';\n";
        php += "$game['ts_game_paused'] = '" + (stackEvents.peek().getEvent() == EVENT_PAUSE ? DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackEvents.peek().getPit()) : "null") + "';\n";
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
