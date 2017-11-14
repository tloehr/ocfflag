package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class Statistics {
    private long time, time_blue, time_red;

    private final Logger logger = Logger.getLogger(getClass());

    public static final int EVENT_STANDBY = 0; // standby nach NONE oder RESET
    public static final int EVENT_START_GAME = 1; // von Standby nach Active
    public static final int EVENT_BLUE_ACTIVATED = 2;
    public static final int EVENT_RED_ACTIVATED = 3;
    public static final int EVENT_GAME_OVER = 4; // wenn die Spielzeit abgelaufen ist
    public static final int EVENT_STOP_GAME = 5; // wenn das spiel vorzeitig beendet wird

    public static final String[] EVENTS = new String[]{"EVENT_STANDBY", "EVENT_START_GAME",
            "EVENT_BLUE_ACTIVATED", "EVENT_RED_ACTIVATED", "EVENT_GAME_OVER", "EVENT_STOP_GAME"};

    public ArrayList<GameEvent> listEvents;
    private int matchid;

    public Statistics() {
        logger.setLevel(Main.getLogLevel());
        listEvents = new ArrayList<>();
        reset();
    }

    public void reset() {
        time = 0l;
        time_blue = 0l;
        time_red = 0l;
        matchid = 0;
        listEvents.clear();
    }

    public void setTimes(int matchid, long time, long time_blue, long time_red) {
        this.matchid = matchid;
        this.time = time;
        this.time_blue = time_blue;
        this.time_red = time_red;
    }

    public void sendStats() {
        logger.debug(String.format("MatchID: %s | Time: %s | Blue: %s | Red: %s",
                matchid,
                Tools.formatLongTime(time),
                Tools.formatLongTime(time_blue),
                Tools.formatLongTime(time_red))
        );
        logger.debug(StringUtils.join(listEvents));
    }

    public void addEvent(int event) {
        logger.debug(EVENTS[event]);
        listEvents.add(new GameEvent(new DateTime(), event));
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
    }

}
