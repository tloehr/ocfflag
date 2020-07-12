package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.misc.Configs;

import java.io.IOException;

public class GameSelector extends Game {
    String[] gamemodes;
    int game_index;

    @Override
    void initGame() {
        set_config_buttons_labels("", "game++", "game--", "CHANGE GAME");
        gamemodes = configs.get(Configs.RLGS_GAMEMODES).split("\\,");
        game_index = 0;
        update_all_signals();
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
        update_all_signals();
    }

    @Override
    void button_k3_pressed() {
        game_index--;
        if (game_index < 0) game_index = gamemodes.length - 1;
        update_all_signals();
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



        } catch (IOException e) {
            getLogger().error(e);
        }
    }

    @Override
    void setFlagSignals() {

    }

    @Override
    void setLEDsAndButtons() {
        set_blinking_red_button("∞:on,500;off,500");
        set_blinking_blue_button("∞:on,500;off,500");
        set_blinking_green_button("∞:on,500;off,500");
        set_blinking_yellow_button("∞:on,500;off,500");
    }

}
