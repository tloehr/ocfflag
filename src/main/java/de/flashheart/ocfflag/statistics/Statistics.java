package de.flashheart.ocfflag.statistics;

import de.flashheart.GameEvent;
import de.flashheart.GameState;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class Statistics implements HasLogger {

    public static final String[] EVENTS_TO_STATE = new String[]{GameEvent.GAME_ABORTED, GameEvent.GAME_OVER};
    private ArrayList<String> winners;
    private GameState gameState;

    public GameState getGameState() {
        return gameState;
    }

    /**
     * @param maxtime maximum gametime in minutes
     */
    public Statistics(long maxtime) {
//        messageProcessor = Main.getMessageProcessor();
        winners = new ArrayList<>();
        reset(maxtime * 60000);
    }

    public void reset(long maxtime) {
        winners.clear();
        gameState = new GameState(Main.getFromConfigs(Configs.FLAGNAME), GameState.TYPE_CENTERFLAG, Main.getFromConfigs(Configs.MYUUID), ((Configs) Main.getFromContext("configs")).getNextMatchID(), maxtime);
    }

    public void updateTimers(long remaining) {
        gameState.setGametime(gameState.getMaxgametime() - remaining);
        gameState.setTimestamp(System.currentTimeMillis());
    }

    public void sendStats() {
        //messageProcessor.pushMessage(gameState);
        // not now. maybe later
    }

    public ArrayList<String> getWinners() {
        return winners;
    }

    public long addEvent(String event, long remaining, LinkedHashMap<String, Integer> teamranking) {
        updateTimers(remaining);

        if (event.equals(GameEvent.GAME_OVER)
                || event.equals(GameEvent.GAME_ABORTED)) {
            gameState.setTimestamp_game_ended(gameState.getTimestamp());
        }

        if (event.equals(GameEvent.GAME_OVER)) {
            winners = GameStateService.getWinners(teamranking);
            if (winners.size() > 1) {
                gameState.getGameEvents().add(new GameEvent(GameEvent.RESULT_MULTI_WINNERS, gameState.getGametime(), teamranking));
                getLogger().info("More than one winner - very rare");
            }
            if (winners.contains("red")) {
                getLogger().info("Red Team won");
                gameState.getGameEvents().add(new GameEvent(GameEvent.RESULT_RED_WON, gameState.getGametime(), teamranking));
            }
            if (winners.contains("blue")) {
                getLogger().info("Blue Team won");
                gameState.getGameEvents().add(new GameEvent(GameEvent.RESULT_BLUE_WON, gameState.getGametime(), teamranking));
            }
            if (winners.contains("green")) {
                getLogger().info("Green Team won");
                gameState.getGameEvents().add(new GameEvent(GameEvent.RESULT_GREEN_WON, gameState.getGametime(), teamranking));
            }
            if (winners.contains("yellow")) {
                getLogger().info("Yellow Team won");
                gameState.getGameEvents().add(new GameEvent(GameEvent.RESULT_YELLOW_WON, gameState.getGametime(), teamranking));
            }
        }

        // Und jetzt erst der eigentliche Event. Dadurch steht Game_OVER oder ABORTED immer am Schluss.
        GameEvent gameEvent = new GameEvent(event, gameState.getGametime(), teamranking);
        gameState.getGameEvents().add(gameEvent);

        // Result ?
        if (Arrays.asList(EVENTS_TO_STATE).contains(event)) {
            gameState.setState(event);
        }

        if (event.equals(GameEvent.FLAG_NEUTRAL)) gameState.setColor("white");
        if (event.equals(GameEvent.RED_ACTIVATED)) gameState.setColor("red");
        if (event.equals(GameEvent.BLUE_ACTIVATED)) gameState.setColor("blue");
        if (event.equals(GameEvent.GREEN_ACTIVATED)) gameState.setColor("green");
        if (event.equals(GameEvent.YELLOW_ACTIVATED)) gameState.setColor("yellow");

        sendStats(); // jedes Ereignis wird gesendet.

        return gameState.getTimestamp();
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
//        String flagname = Main.getFromConfigs(Configs.FLAGNAME);
//
//        flagname = StringUtils.replace(flagname, "'", "\\'");
//        flagname = StringUtils.replace(flagname, "\"", "\\\"");
//
//        php.append("$game['flagname'] = '" + flagname + "';\n");
//        php.append("$game['flagcolor'] = '" + flagcolor + "';\n");
//        php.append("$game['uuid'] = '" + Main.getFromConfigs(Configs.MYUUID) + "';\n");
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
