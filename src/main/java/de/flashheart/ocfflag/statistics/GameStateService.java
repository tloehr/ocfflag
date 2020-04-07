package de.flashheart.ocfflag.statistics;

import de.flashheart.GameState;
import org.apache.commons.collections4.IteratorUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameStateService {
    /**
     * die (vorraussichtlich) restliche Spielzeit. 0, wenn beendet. hängt vom gametype ab, ob es eine Schätzung oder präzise ist.
     *
     * @param gameState
     * @return
     */
    public static long getRemaining(GameState gameState) {
        return gameState.getMaxgametime() - gameState.getGametime();
    }

    /**
     * wenn alle rankings den rang 1 haben, müssen alle teams gleich gespielt haben.
     *
     * @return
     */
    public static boolean isDrawgame(GameState gameState) {
        // lastElement() replacement
        LinkedHashMap<String, Integer> rank = IteratorUtils.get(gameState.getGameEvents().iterator(), gameState.getGameEvents().size() - 1).getTeamranking();
        return rank.values().stream()
                .distinct().count() == 1; // ermittelt ob alle Werte in der Map gleich sind.
    }

    public static ArrayList<String> getWinners(LinkedHashMap<String, Integer> teamranking) {
        ArrayList<String> winners = new ArrayList<>();
        Integer maxtime = teamranking.entrySet().stream().max(Map.Entry.comparingByValue()).get().getValue();
        teamranking.entrySet().stream().forEach(stringIntegerEntry -> {
            if (stringIntegerEntry.getValue().equals(maxtime)) winners.add(stringIntegerEntry.getKey());
        });
        return winners;
    }


}
