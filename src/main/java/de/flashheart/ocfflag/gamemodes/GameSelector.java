package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.PinHandler;
import de.flashheart.ocfflag.hardware.RGBScheduleElement;
import de.flashheart.ocfflag.misc.Configs;

import java.awt.*;
import java.io.IOException;

public class GameSelector extends Game {
    String[] gamemodes;
    int game_index;

    @Override
    void initGame() {
        set_config_buttons_labels("", "game++", "game--", "CHANGE GAME");
        lcdTextDisplay.update_page(0, "Welcome to the", "Real Life", "Gaming System", "");
        ledTextDisplay.setText("Select a game");
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
        return "Game Selector";
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

            if (gamemodes[game_index].equalsIgnoreCase("ocf2")) ledTextDisplay.setText("Operation Cedar Falls 2 Teams");
            if (gamemodes[game_index].equalsIgnoreCase("ocf3")) ledTextDisplay.setText("Operation Cedar Falls 3 Teams");
            if (gamemodes[game_index].equalsIgnoreCase("ocf4")) ledTextDisplay.setText("Operation Cedar Falls 4 Teams");
            if (gamemodes[game_index].equalsIgnoreCase("spwn")) ledTextDisplay.setText("Spawn Counter");

//            ledTextDisplay.setText("Sixteen Chars 16");
        } catch (IOException e) {
            getLogger().error(e);
        }
    }

    @Override
    void setFlagSignals() {
        set_blinking_flag_white("∞:off,0;on,350;off,6400");
        set_blinking_flag_red("∞:off,350;on,350;off,6050");
        set_blinking_flag_blue("∞:off,700;on,350;off,5700");
        set_blinking_flag_green("∞:off,1050;on,350;off,5350");
        set_blinking_flag_yellow("∞:off,1400;on,350;off,5000");

        String pregamePoleColorScheme = PinHandler.FOREVER + ":" +
                new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 350l) + ";" +
                new RGBScheduleElement(Configs.FLAG_RGB_RED, 350l) + ";" +
                new RGBScheduleElement(Configs.FLAG_RGB_BLUE, 350l) + ";" +
                new RGBScheduleElement(Configs.FLAG_RGB_GREEN, 350l) + ";" +
                new RGBScheduleElement(Configs.FLAG_RGB_YELLOW, 350l) + ";" +
                new RGBScheduleElement(Color.BLACK, 5000l);

        set_blinking_flag_rgb(pregamePoleColorScheme);
    }

    @Override
    void setLEDsAndButtons() {
        set_blinking_red_button("∞:on,500;off,500");
        set_blinking_blue_button("∞:on,500;off,500");
        set_blinking_green_button("∞:on,500;off,500");
        set_blinking_yellow_button("∞:on,500;off,500");
    }

}
