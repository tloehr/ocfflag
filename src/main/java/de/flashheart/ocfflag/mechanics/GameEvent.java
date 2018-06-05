package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.statistics.Statistics;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class GameEvent {

    private DateTime pit;
    private int event;

    public GameEvent(DateTime pit, int event) {
        this.pit = pit;
        this.event = event;
    }

    public DateTime getPit() {
        return pit;
    }

    public int getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "pit=" + pit.toString(DateTimeFormat.mediumDateTime()) +
                ", event=" + Statistics.EVENTS[event] +
                '}';
    }

    public String toPHPArray() {
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','event' => '" + Statistics.EVENTS[event] + "'],\n";
    }

}
