package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.statistics.Statistics;
import org.joda.time.DateTime;

import java.util.LinkedHashMap;

public class GameEvent {

    private final DateTime pit;
    private final long time;
    private final LinkedHashMap<String, Integer> teams;
    private final int event;


    public GameEvent(DateTime pit, long time, LinkedHashMap<String, Integer> teams, int event) {
        this.pit = pit;
        this.time = time;
        this.teams = teams;
        this.event = event;
    }

    public DateTime getPit() {
        return pit;
    }

    public int getEvent() {
        return event;
    }

    public long getTime() {
        return time;
    }

    public LinkedHashMap<String, Integer> getTeams() {
        return teams;
    }

    @Override
    public String toString() {
        return "GameEvent{" +
                "pit=" + pit +
                ", time=" + time +
                ", teams=" + teams +
                ", event=" + event +
                '}';
    }

    public String toPHPArray() {
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','longtime' => '" + time + "','humantime' => '" + Tools.formatLongTime(time, "mm:ss") + "','event' => '" + Statistics.EVENTS[event] + "'],\n";
    }


}
