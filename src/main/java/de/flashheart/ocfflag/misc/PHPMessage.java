package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.mechanics.GameEvent;

public class PHPMessage {
    String php;

    GameEvent gameEvent;

    public PHPMessage(String php, GameEvent gameEvent) {
        this.php = php;
        this.gameEvent = gameEvent;

    }

    public String getPhp() {
        return php;
    }


    public GameEvent getGameEvent() {
        return gameEvent;
    }
}
