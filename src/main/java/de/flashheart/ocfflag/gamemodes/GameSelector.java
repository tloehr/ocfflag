package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;

import java.io.IOException;

public class GameSelector extends Game {
    String[] gamemodes;
    //    final int OCF2 = 0;
//    final int OCF3 = 1;
//    final int OCF4 = 2;
//    final int SPWN = 3;
    int game_index;

    @Override
    void initGame() {
        k1.setText(" ");
        k2.setText("game++");
        k3.setText("game--");
        k4.setText("CHANGE GAME");
        gamemodes = configs.get(Configs.RLGS_GAMEMODES).split("\\,");
        game_index = 0;
        setDisplay();
    }

    @Override
    void button_k4_pressed() {
        Game game = this;
        if (gamemodes[game_index].equalsIgnoreCase("ocf2")) game = new OCF(2);
        if (gamemodes[game_index].equalsIgnoreCase("ocf3")) game = new OCF(3);
        if (gamemodes[game_index].equalsIgnoreCase("ocf4")) game = new OCF(4);
        if (gamemodes[game_index].equalsIgnoreCase("spwn")) game = new SpawnCounter();
        Main.setGame(game);
    }

    @Override
    void button_k2_pressed() {
        game_index++;
        if (game_index >= gamemodes.length) game_index = 0;
        setDisplay();
    }

    @Override
    void button_k3_pressed() {
        game_index--;
        if (game_index < 0) game_index = gamemodes.length - 1;
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
            getLogger().debug(gamemodes[game_index]);
            display_white.setText(gamemodes[game_index]);

            set_blinking_red_button("∞:on,500;off,500");
            set_blinking_blue_button("∞:on,500;off,500");

        } catch (IOException e) {
            getLogger().error(e);
        }
    }

}
