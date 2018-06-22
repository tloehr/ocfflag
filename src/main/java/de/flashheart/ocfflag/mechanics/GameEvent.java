package de.flashheart.ocfflag.mechanics;

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
        return "   ['pit' => '" + pit.toString("HH:mm:ss") + "','time' => '" + time + "','event' => '" + Statistics.EVENTS[event] + "'],\n";
    }

    /**
     * Das Problem mit dem Zurücksetzen sind die Zeiten eines Events. Wenn also z.B. die Flagge versehentlich von Rot nach Grün geschaltet
     * wurde, was wir hier zurück nehmen wollen. Dann stehen die Zeiten (Teams und Game) auf ANFANG des Events und nicht auf "kurz vor der Fehlbedienung"
     * Daher muss der Rücksetzpunkt eine Kombination aus vorherigem und falschem Event sein.
     * @param eventToUndo Event, der nicht hätte sein sollen.
     * @param revertToEvent Event, zu dem wir zurück springen wollen.
     * @return Event to jump back to
     */
    public static GameEvent createRevertableEvent(GameEvent revertToEvent, GameTimes eventToUndo){
        
    }

}
