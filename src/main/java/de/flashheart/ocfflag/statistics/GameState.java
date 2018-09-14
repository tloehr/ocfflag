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

    private String bombname;
    private String gametype;
    private String state;
    private String uuid;
    private long matchid;
    private long timestamp;
    private long timestamp_game_started;
    private long timestamp_game_paused;
    private long timestamp_game_ended;
    private boolean bombfused;
    private long remaining;
    private long capturetime;
    private long maxgametime;
    private long gametime;
    private List<GameEvent> gameEvents;
    private String zoneid; // for timezone handling
    private String color;
    private LinkedHashMap<String, Integer> teamranking;
    private long numteams;

    public GameState() {
        gameEvents = new ArrayList<>();
        teamranking = new LinkedHashMap<>();
    }

    public GameState(String bombname, String gametype, String uuid, long matchid, long numteams, long maxgametime) {
        this();
        this.numteams = numteams;
        this.state = GameEvent.PREGAME;
        this.bombfused = false;
        this.bombname = bombname;
        this.gametype = gametype;
        this.uuid = uuid;
        this.matchid = matchid;
        this.capturetime = 0l;
        this.maxgametime = maxgametime;
        this.remaining = 0l;
        this.bombfused = false;
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

    public boolean isBombfused() {
        return bombfused;
    }

    public void setBombfused(boolean bombfused) {
        this.bombfused = bombfused;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getCapturetime() {
        return capturetime;
    }

    public void setCapturetime(long capturetime) {
        this.capturetime = capturetime;
    }

    public long getMaxgametime() {
        return maxgametime;
    }

    public void setMaxgametime(long maxgametime) {
        this.maxgametime = maxgametime;
    }

    public long getGametime() {
        return gametime;
    }

    public void setGametime(long gametime) {
        this.gametime = gametime;
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

    public LinkedHashMap<String, Integer> getTeamranking() {
        return teamranking;
    }

    public void setTeamranking(LinkedHashMap<String, Integer> teamranking) {
        this.teamranking = teamranking;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getNumteams() {
        return numteams;
    }

    public void setNumteams(long numteams) {
        this.numteams = numteams;
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
                ", bombfused=" + bombfused +
                ", remaining=" + remaining +
                ", capturetime=" + capturetime +
                ", maxgametime=" + maxgametime +
                ", gametime=" + gametime +
                ", gameEvents=" + gameEvents +
                ", zoneid='" + zoneid + '\'' +
                ", color='" + color + '\'' +
                ", teamranking=" + teamranking +
                ", numteams=" + numteams +
                '}';
    }

}
