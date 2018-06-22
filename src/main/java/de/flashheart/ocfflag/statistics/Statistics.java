package de.flashheart.ocfflag.statistics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.mechanics.GameEvent;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.swing.*;
import java.util.*;
import java.util.stream.IntStream;

public class Statistics implements HasLogger {

    private final int numTeams;
    private long time;

    private LinkedHashMap<String, Integer> rank;

    public static final int EVENT_PAUSE = 0;
    public static final int EVENT_RESUME = 1;
    public static final int EVENT_FLAG_NEUTRAL = 2; // von Standby nach Active. Flagge ist Neutral.
    public static final int EVENT_BLUE_ACTIVATED = 3;
    public static final int EVENT_RED_ACTIVATED = 4;
    public static final int EVENT_GAME_OVER = 5; // wenn die Spielzeit abgelaufen ist
    public static final int EVENT_GAME_ABORTED = 6; // wenn das Spiel beendet wurde
    public static final int EVENT_RESULT_RED_WON = 7;
    public static final int EVENT_RESULT_BLUE_WON = 8;
    public static final int EVENT_RESULT_DRAW = 9; // Unentschieden
    public static final int EVENT_YELLOW_ACTIVATED = 10;
    public static final int EVENT_GREEN_ACTIVATED = 11;
    public static final int EVENT_RESULT_GREEN_WON = 12;
    public static final int EVENT_RESULT_YELLOW_WON = 13;
    public static final int EVENT_RESULT_MULTI_WINNERS = 14; // wenn mehr als einer die bestzeit erreicht hat (seeeeehr unwahrscheinlich)
    public static final int EVENT_REVERT_LAST_EVENT = 15;
    public static final int[] GAME_RELEVANT_EVENTS = new int[]{EVENT_FLAG_NEUTRAL, EVENT_GREEN_ACTIVATED, EVENT_RED_ACTIVATED, EVENT_BLUE_ACTIVATED, EVENT_YELLOW_ACTIVATED};

    public static final String[] EVENTS = new String[]{"EVENT_PAUSE", "EVENT_RESUME", "EVENT_FLAG_NEUTRAL",
            "EVENT_BLUE_ACTIVATED", "EVENT_RED_ACTIVATED", "EVENT_GAME_OVER", "EVENT_GAME_ABORTED",
            "EVENT_RESULT_RED_WON", "EVENT_RESULT_BLUE_WON", "EVENT_RESULT_DRAW", "EVENT_YELLOW_ACTIVATED",
            "EVENT_GREEN_ACTIVATED", "EVENT_RESULT_GREEN_WON", "EVENT_RESULT_YELLOW_WON", "EVENT_RESULT_MULTI_WINNERS"};

    private Deque<GameEvent> stackDeque;
    private int matchid;
    private DateTime endOfGame = null;
    private ArrayList<String> winningTeams = new ArrayList<>();
    private SwingWorker<Boolean, Boolean> worker;
    private long min_stat_send_time;
    private String flagcolor;
    private final MessageProcessor messageProcessor;


    public Statistics(int numTeams) {
        messageProcessor = Main.getMessageProcessor();
        this.numTeams = numTeams;
        rank = new LinkedHashMap<>();
        stackDeque = new ArrayDeque<>();
        reset();
    }

    public void reset() {
        min_stat_send_time = Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME));
        endOfGame = null;
        flagcolor = "neutral";
        winningTeams.clear();
        rank.clear();
        time = 0l;

        rank.put("red", 0);
        rank.put("blue", 0);
        if (numTeams >= 3) rank.put("green", 0);
        if (numTeams >= 4) rank.put("yellow", 0);

        matchid = 0;
        stackDeque.clear();
        messageProcessor.cleanupStatsFile();
    }

    public void setTimes(int matchid, long time, LinkedHashMap<String, Integer> myrank) {
        this.matchid = matchid;
        this.time = time;
        this.rank = myrank; //Hier stehen die aktuellen Zeiten einzelnen Teams drin.
        getLogger().debug(time);
    }

    /**
     * @return true, wenn die Operation erfolgreich war.
     */
    public void sendStats() {
        getLogger().debug("sendStats()\n" + toPHP());
        if (min_stat_send_time > 0)
            messageProcessor.pushMessage(new PHPMessage(toPHP(), stackDeque.peek()));
    }

    public long addEvent(int event) {
        DateTime now = new DateTime();
//        if (Long.parseLong(Main.getConfigs().get(Configs.MIN_STAT_SEND_TIME)) > 0) return now.getMillis();

        stackDeque.push(new GameEvent(now, time, rank, event));

        if (endOfGame == null) {
            if (event == EVENT_GAME_ABORTED || event == EVENT_GAME_OVER) {
                endOfGame = now;
            }
        }

        if (event == EVENT_RESULT_RED_WON) winningTeams.add("red");
        if (event == EVENT_RESULT_BLUE_WON) winningTeams.add("blue");
        if (event == EVENT_RESULT_GREEN_WON) winningTeams.add("green");
        if (event == EVENT_RESULT_YELLOW_WON) winningTeams.add("yellow");

        if (event == EVENT_RED_ACTIVATED) flagcolor = "red";
        if (event == EVENT_BLUE_ACTIVATED) flagcolor = "blue";
        if (event == EVENT_GREEN_ACTIVATED) flagcolor = "green";
        if (event == EVENT_YELLOW_ACTIVATED) flagcolor = "yellow";

        sendStats(); // jedes Ereignis wird gesendet.

        return now.getMillis();
    }

    /**
     * @return die letzten beiden Spiel-Relevanten Events. Wenn es nur einen gibt, dann ist noch nichts passiert, außer dass das Spiel angefangen hat. Die Flagge ist dann noch neutral.
     */
    public Optional<GameEvent> getLastRevertableEvent() {
        GameEvent[] gameEvents = stackDeque.stream().filter(gameEvent -> IntStream.of(GAME_RELEVANT_EVENTS).anyMatch(x -> x == gameEvent.getEvent())).limit(2).toArray(GameEvent[]::new);
        Optional<GameEvent> myEvent = Optional.ofNullable(gameEvents.length == 2 ? gameEvents[1] : null);
        return myEvent;
    }


    private String toPHP() {


        final StringBuilder php = new StringBuilder();
        php.append("<?php\n");

        String flagname = Main.getConfigs().get(Configs.FLAGNAME);

        flagname = StringUtils.replace(flagname, "'", "\\'");
        flagname = StringUtils.replace(flagname, "\"", "\\\"");

        php.append("$game['flagname'] = '" + flagname + "';\n");
        php.append("$game['flagcolor'] = '" + flagcolor + "';\n");
        php.append("$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n");
        php.append("$game['matchid'] = '" + matchid + "';\n");
        php.append("$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n");
        php.append("$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackDeque.getFirst().getPit()) + "';\n");
        php.append("$game['ts_game_paused'] = '" + (stackDeque.peek().getEvent() == EVENT_PAUSE ? DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackDeque.peek().getPit()) : "null") + "';\n");
        php.append("$game['ts_game_ended'] = '" + (endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame)) + "';\n");
        php.append("$game['time'] = '" + Tools.formatLongTime(time, "HH:mm:ss") + "';\n");
        php.append("$game['num_teams'] = '" + numTeams + "';\n");


        php.append("$game['rank'] = [\n");

        rank.entrySet().stream()
                .forEach(stringIntegerEntry -> {
                    php.append("   '" + stringIntegerEntry.getKey() + "' => '" + Tools.formatLongTime(stringIntegerEntry.getValue() * 1000, "HH:mm:ss") + "',\n");
                });

        php.append("];\n");

        php.append("$game['winning_teams'] = [\n");
        if (winningTeams.isEmpty()) {
            php.append(endOfGame == null ? "'notdecidedyet'" : "'drawgame'");
        } else {
            for (String team : winningTeams) {
                php.append("'" + team + "',");
            }
        }
        php.append("];\n");

        php.append("$game['events'] = [\n");
        for (GameEvent event : stackDeque) {
            php.append(event.toPHPArray());
        }

        php.append("];\n?>");


        return php.toString();
    }


}
