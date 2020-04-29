package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;

import java.io.IOException;

public class GameSelector extends Game {
    String[] gamemodes = new String[]{"OCF2", "OCF3", "OCF4", "SPWN"};
    final int OCF2 = 0;
    final int OCF3 = 1;
    final int OCF4 = 2;
    final int SPWN = 3;
    int game_index;

    @Override
    void initGame() {
        k1.setText("Run selected game");
        k3.setText("game++");
        k4.setText("game--");
        game_index = 0;
    }

    @Override
    void button_teamcolor_pressed(String FLAGSTATE) {
        // i dont care about these buttons here
    }

    @Override
    void button_k1_pressed() {
        Game game = null;
        switch (game_index) {
            case OCF2: {
                game = new OCF(2);
                break;
            }
            case OCF3: {
                game = new OCF(3);
                break;
            }
            case OCF4: {
                game = new OCF(4);
                break;
            }
            case SPWN: {
                game = new SpawnCounter();
                break;
            }
            default: {

            }
        }
        Main.setGame(game);
    }

    @Override
    void button_k3_pressed() {
        game_index++;
        if (game_index >= gamemodes.length) game_index = 0;
        setDisplay();
    }

    @Override
    void button_k4_pressed() {
        game_index--;
        if (game_index <= 0) game_index = gamemodes.length - 1;
        setDisplay();
    }

    @Override
    public String getName() {
        return "GameSelector";
    }

    @Override
    public boolean isGameRunning() {
        return true;
    }

    @Override
    void setDisplay() {
        try {
            display_white.setText(gamemodes[game_index]);
        } catch (IOException e) {
            getLogger().error(e);
        }
    }

    @Override
    void setFlagSignals() {
        
    }

    @Override
    void setLEDsAndButtons() {
        
    }
}
