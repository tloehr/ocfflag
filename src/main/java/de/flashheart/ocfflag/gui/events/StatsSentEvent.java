package de.flashheart.ocfflag.gui.events;

import de.flashheart.ocfflag.statistics.GameState;

import java.util.EventObject;

public class StatsSentEvent extends EventObject {
    private final boolean successful;
    private final GameState gameState;

    public StatsSentEvent(Object source, GameState gameState, boolean successful) {
        super(source);
        this.gameState = gameState;

        this.successful = successful;

    }

    public boolean isSuccessful() {
        return successful;
    }

    public GameState getGameState() {
        return gameState;
    }
}
