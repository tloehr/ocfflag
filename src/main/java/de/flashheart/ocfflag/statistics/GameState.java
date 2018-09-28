package de.flashheart.ocfflag.statistics;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class GameState {

    // for gametype
    public static final String TYPE_FARCRY = "farcry";
    public static final String TYPE_CENTERFLAG = "centerflag";

    /**
     * Hier steht der Name der Box / Bombe. Kann frei gewählt werden.
     */
    private String bombname;
    /**
     * entsprechend den obigen TYPE Konstanten
     */
    private String gametype;
    /**
     * State enthält immer der aktuellen Zustand. Das sind ausgewählte GameEvent Bezeichnungen, die zuletzt eingetreten sind.
     * Nicht alle werden zu einem State. Welche das sind hängt von der jeweiligen Statistics Klasse ab.
     */
    private String state;
    /**
     * die UUID ist ein automatisch errechneter Primärschlüssel für jede Box-Software die eingerichtet wird.
     * Das RLG System geht davon aus, dass es sich wirklich um einen Primärschlüssel handelt. Obwohl man
     * den ändern könnten. Aber dann passieren lustige Sachen, die wir nicht wollen.
     */
    private String uuid;
    /**
     * jede Box nummeriert die einzelnen Matches durch indem es einen Zähler führt.
     */
    private long matchid;
    /**
     * Das ist der Timestamp, an dem dieser GameState erzeugt wurde.
     */
    private long timestamp;
    /**
     * Spielbeginn, Spielende (wenn schon beendet, sonst -1) und Beginn des Pausezustandes (wenn in Pause, sonst -1)
     */
    private long timestamp_game_started;
    private long timestamp_game_paused;
    private long timestamp_game_ended;
    /**
     * die aktuelle Spielzeit. Beginnen bei 0.
     */
    private long gametime;
    /**
     * Maximale Zeit, die das Spiel dauern kann. Evtl. mit potenziellen Overtimes. Es muss also nicht notwendigerweise
     * wirklich so lange dauern. Hängt vom gametype ab.
     */
    private long maxgametime;
    /**
     * Das ist die Liste der einzelnen GameEvents, die schon stattgefunden haben und die letztlich zu diesem, aktuellen
     * State geführt haben.
     */
    private List<GameEvent> gameEvents;
    /**
     * Das hier habe zur korrekten Weiterverarbeitung eingebaut. Die Zeiten beziehen sich natürlich immer auf die
     * lokale Zeitzone. Ich glaube ja nicht, dass wir sobald Zeitzonen-Übergreifende Events veranstalten werden,
     * aber wenn, dann stimmt auch die Berechnung.
     *
     * Das ist die Zoneid der jeweiligen Box, deren Zustand hier beschrieben wird.
     */
    private String zoneid;
    /**
     * Es spielen fast immer Farben eine Rolle. Eine Flagge leuchtet, ein Knopf wird umgelegt. usw.
     * Damit das bei der späteren Webdarstellung direkt abgegriffen werden kann steht hier
     * der HTML Color code als String drin. Auf mit den Textkonstanten wie WHITE, BLUE, RED usw.
     */
    private String color;

    public GameState() {
        gameEvents = new ArrayList<>();
    }

    public GameState(String bombname, String gametype, String uuid, long matchid, long maxgametime) {
        this();
        this.state = GameEvent.PREGAME;
        this.bombname = bombname;
        this.gametype = gametype;
        this.uuid = uuid;
        this.matchid = matchid;
        this.maxgametime = maxgametime;
        this.gametime = 0l;
        this.timestamp_game_started = -1;
        this.timestamp_game_paused = -1;
        this.timestamp_game_ended = -1;
        this.timestamp = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.zoneid = ZoneId.systemDefault().getId();
        this.color = "white";
    }

    public String getBombname() {
        return bombname;
    }

    public void setBombname(String bombname) {
        this.bombname = bombname;
    }

    public String getGametype() {
        return gametype;
    }

    public void setGametype(String gametype) {
        this.gametype = gametype;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getMatchid() {
        return matchid;
    }

    public void setMatchid(long matchid) {
        this.matchid = matchid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp_game_started() {
        return timestamp_game_started;
    }

    public void setTimestamp_game_started(long timestamp_game_started) {
        this.timestamp_game_started = timestamp_game_started;
    }

    public long getTimestamp_game_paused() {
        return timestamp_game_paused;
    }

    public void setTimestamp_game_paused(long timestamp_game_paused) {
        this.timestamp_game_paused = timestamp_game_paused;
    }

    public long getTimestamp_game_ended() {
        return timestamp_game_ended;
    }

    public void setTimestamp_game_ended(long timestamp_game_ended) {
        this.timestamp_game_ended = timestamp_game_ended;
    }


    public long getGametime() {
        return gametime;
    }

    public void setGametime(long gametime) {
        this.gametime = gametime;
    }

    public long getMaxgametime() {
        return maxgametime;
    }

    public void setMaxgametime(long maxgametime) {
        this.maxgametime = maxgametime;
    }


    public List<GameEvent> getGameEvents() {
        return gameEvents;
    }

    public void setGameEvents(List<GameEvent> gameEvents) {
        this.gameEvents = gameEvents;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZoneid() {
        return zoneid;
    }

    public void setZoneid(String zoneid) {
        this.zoneid = zoneid;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "GameState{" +
                "bombname='" + bombname + '\'' +
                ", gametype='" + gametype + '\'' +
                ", state='" + state + '\'' +
                ", uuid='" + uuid + '\'' +
                ", matchid=" + matchid +
                ", timestamp=" + timestamp +
                ", timestamp_game_started=" + timestamp_game_started +
                ", timestamp_game_paused=" + timestamp_game_paused +
                ", timestamp_game_ended=" + timestamp_game_ended +
                ", gametime=" + gametime +
                ", maxgametime=" + maxgametime +
                ", gameEvents=" + gameEvents +
                ", zoneid='" + zoneid + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
