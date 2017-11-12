package de.flashheart.ocfflag.mechanics;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class Statistics {
    private long time, time_blue, time_red;

    public static final int EVENT_GAME_START = 0;
    public static final int EVENT_BLUE_ACTIVATED = 1;
    public static final int EVENT_RED_ACTIVATED = 2;
    public static final int EVENT_GAME_PAUSED = 3;
    public static final int EVENT_GAME_ACTIVATED = 4;
    public static final int EVENT_GAME_OVER = 5;
    public static final int EVENT_GAME_END = 10;

    public ArrayList<GameEvent> listEvents;

    public Statistics() {
        listEvents = new ArrayList<>();
        resetStats();
    }

    public void resetStats() {
        time = 0l;
        time_blue = 0l;
        time_red = 0l;
        listEvents.clear();
    }

    public void sendStats() {
        // noch nicht

    }

    public void addEvent(int event) {
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
