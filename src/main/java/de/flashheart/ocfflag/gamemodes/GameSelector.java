package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;

import java.io.IOException;

public class GameSelector extends GameMode {
    String[] gamemodes = new String[]{"OCF2", "OCF3", "OCF4", "SPWN"};
    final int OCF2 = 0;
    final int OCF3 = 1;
    final int OCF4 = 2;
    final int SPWN = 3;
    int game_index;

    public GameSelector() {
        super();
        game_index = 0;
    }

    @Override
    void initHardware() {
        super.initHardware();

        k1.setText("Run selected game");
        k3.setText("game++");
        k4.setText("game--");

    }

    @Override
    void button_k1_pressed() {
        GameMode gameMode = null;
        switch (game_index){
            case OCF2 : {
                gameMode = new OCF(2);
                break;
            }
            case OCF3 : {
                gameMode = new OCF(3);
                break;
            }
            case OCF4 : {
                gameMode = new OCF(4);
                break;
            }
            case SPWN : {
                gameMode = new SpawnCounter();
                break;
            }
            default:{

            }
        }
        Main.setGame(gameMode);
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

    private void setDisplay() {
        try {
            display_white.setText(gamemodes[game_index]);
        } catch (IOException e) {
            getLogger().error(e);
        }
    }


}
