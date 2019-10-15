package de.flashheart.ocfflag.mechanics;

public class GameSelector extends Games {

    public GameSelector() {
        super();
    }

    @Override
    public String getName() {
        return "GameSelector";
    }

    @Override
    public String getShortID() {
        return "SEL";
    }

    @Override
    public boolean isGameRunning() {
        return false;
    }
}
