package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;
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

    public Statistics() {
        logger.setLevel(Main.getLogLevel());
        stackEvents = new Stack<>();
        reset();
    }

    public void reset() {

        if (!stackEvents.isEmpty()) {
            logger.debug("CLOSE AND SEND Statistics list");
        }

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
//        logger.debug(String.format("MatchID: %s | Time: %s | Blue: %s | Red: %s",
//                matchid,
//                Tools.formatLongTime(time),
//                Tools.formatLongTime(time_blue),
//                Tools.formatLongTime(time_red))
//        );
    }

    public void sendStats() {
//        logger.debug(String.format("MatchID: %s | Time: %s | Blue: %s | Red: %s",
//                matchid,
//                Tools.formatLongTime(time),
//                Tools.formatLongTime(time_blue),
//                Tools.formatLongTime(time_red))
//        );
        logger.debug(toPHP());
    }

    public void addEvent(int event) {
        logger.debug(EVENTS[event]);
        stackEvents.add(new GameEvent(new DateTime(), event));
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
        php += "$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n";
        php += "$game['matchid'] = '" + matchid + "';\n";
        php += "$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n";
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
