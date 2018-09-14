package de.flashheart.ocfflag.statistics;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class Statistics implements HasLogger {

    public static final String[] EVENTS_TO_STATE = new String[]{GameEvent.GAME_ABORTED, GameEvent.RESULT_BLUE_WON, GameEvent.RESULT_RED_WON, GameEvent.RESULT_YELLOW_WON, GameEvent.RESULT_GREEN_WON};


    private GameState gameState;
//    protected LinkedHashMap<String, Integer> teams = null;

    private final MessageProcessor messageProcessor;


    public Statistics(long numTeams, long maxtime) {
        messageProcessor = Main.getMessageProcessor();
        reset(numTeams, maxtime);
    }

    public void reset(long numTeams, long maxtime) {
        gameState = new GameState(Main.getConfigs().get(Configs.FLAGNAME), GameState.TYPE_CENTERFLAG, Main.getConfigs().get(Configs.MYUUID), Main.getConfigs().getNextMatchID(), numTeams, maxtime);
    }

    public void updateTimers(long now, long timer, LinkedHashMap<String, Integer> rank) {
        gameState.setGametime(timer);
        gameState.setTeamranking(rank);
        gameState.setTimestamp(now);
    }

    public void sendStats() {
        messageProcessor.pushMessage(gameState);
    }

    public long addEvent(GameEvent gameEvent) {
        long now = System.currentTimeMillis();
        updateTimers(now, gameEvent.getGametime(), gameEvent.getTeamranking());
        gameState.getGameEvents().add(gameEvent);

        if (gameEvent.getEvent().equals(GameEvent.GAME_OVER)
                || gameEvent.getEvent().equals(GameEvent.GAME_ABORTED)) {
            gameState.setTimestamp_game_ended(now);
        }

        // Result ?
        if (Arrays.asList(EVENTS_TO_STATE).contains(gameEvent.getEvent())) {
            gameState.setState(gameEvent.getEvent());
        }

        if (gameEvent.getEvent().equals(GameEvent.RED_ACTIVATED)) gameState.setColor("red");
        if (gameEvent.getEvent().equals(GameEvent.BLUE_ACTIVATED)) gameState.setColor("blue");
        if (gameEvent.getEvent().equals(GameEvent.GREEN_ACTIVATED)) gameState.setColor("green");
        if (gameEvent.getEvent().equals(GameEvent.YELLOW_ACTIVATED)) gameState.setColor("yellow");

        sendStats(); // jedes Ereignis wird gesendet.

        return now;
    }

//    /**
//     * @return die letzten beiden Spiel-Relevanten Events. Wenn es nur einen gibt, dann ist noch nichts passiert, außer dass das Spiel angefangen hat. Die Flagge ist dann noch neutral.
//     */
////    public Optional<GameEvent> getLastRevertableEvent() {
////        GameEvent[] gameEvents = stackDeque.stream().filter(gameEvent -> IntStream.of(GAME_RELEVANT_EVENTS).anyMatch(x -> x == gameEvent.getEvent())).limit(2).toArray(GameEvent[]::new);
////        Optional<GameEvent> myEvent = Optional.ofNullable(gameEvents.length == 2 ? gameEvents[1] : null);
////        return myEvent;
////    }
//    private String toPHP() {
//
//
//        final StringBuilder php = new StringBuilder();
//        php.append("<?php\n");
//
//        String flagname = Main.getConfigs().get(Configs.FLAGNAME);
//
//        flagname = StringUtils.replace(flagname, "'", "\\'");
//        flagname = StringUtils.replace(flagname, "\"", "\\\"");
//
//        php.append("$game['flagname'] = '" + flagname + "';\n");
//        php.append("$game['flagcolor'] = '" + flagcolor + "';\n");
//        php.append("$game['uuid'] = '" + Main.getConfigs().get(Configs.MYUUID) + "';\n");
//        php.append("$game['matchid'] = '" + matchid + "';\n");
//        php.append("$game['timestamp'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(new DateTime()) + "';\n");
//        php.append("$game['ts_game_started'] = '" + DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackDeque.getFirst().getPit()) + "';\n");
//        php.append("$game['ts_game_paused'] = '" + (stackDeque.peek().getEvent() == EVENT_PAUSE ? DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(stackDeque.peek().getPit()) : "null") + "';\n");
//        php.append("$game['ts_game_ended'] = '" + (endOfGame == null ? "null" : DateTimeFormat.mediumDateTime().withLocale(Locale.getDefault()).print(endOfGame)) + "';\n");
//        php.append("$game['time'] = '" + Tools.formatLongTime(time, "HH:mm:ss") + "';\n");
//        php.append("$game['num_teams'] = '" + numTeams + "';\n");
//
//
//        php.append("$game['rank'] = [\n");
//
//        rank.entrySet().stream()
//                .forEach(stringIntegerEntry -> {
//                    php.append("   '" + stringIntegerEntry.getKey() + "' => '" + Tools.formatLongTime(stringIntegerEntry.getValue() * 1000, "HH:mm:ss") + "',\n");
//                });
//
//        php.append("];\n");
//
//        php.append("$game['winning_teams'] = [\n");
//        if (winningTeams.isEmpty()) {
//            php.append(endOfGame == null ? "'notdecidedyet'" : "'drawgame'");
//        } else {
//            for (String team : winningTeams) {
//                php.append("'" + team + "',");
//            }
//        }
//        php.append("];\n");
//
//        php.append("$game['events'] = [\n");
//        for (GameEvent event : stackDeque) {
//            php.append(event.toPHPArray());
//        }
//
//        php.append("];\n?>");
//
//
//        return php.toString();
//    }


}
