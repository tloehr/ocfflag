package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.PinHandler;
import de.flashheart.ocfflag.hardware.RGBScheduleElement;
import de.flashheart.ocfflag.misc.Configs;

import java.awt.*;
import java.io.IOException;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 */
public class SpawnCounter extends Game {
    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getFromConfigs(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);
    private int spawn_counter = 100;

    public SpawnCounter() {
        super(1);
    }


    @Override
    void init_hardware() {
        super.init_hardware();
        set_config_buttons_labels("C reset/=0", "B +10", "D +100", "CHANGE GAME");
        spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
    }


    @Override
    void init_game() {
        button_quit.setActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });

        update_all_signals();
    }

    @Override
    void button_k2_pressed() {
        super.button_k2_pressed();
        spawn_counter += 10;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        update_all_signals();
    }

    @Override
    void button_k3_pressed() {
        super.button_k3_pressed();
        spawn_counter += 100;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        update_all_signals();
    }

    @Override
    void button_k1_pressed() {
        super.button_k1_pressed();
        if (spawn_counter == configs.getInt(Configs.SPWN_START_TICKETS)) spawn_counter = 0;
        else spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
        update_all_signals();
    }

    @Override
    void button_teamcolor_pressed(String FLAGSTATE) {
        if (spawn_counter == 0) return;
        if (spawn_counter == 1) {
            spawn_counter = 0;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_NOMORETICKETS));
        } else {
            spawn_counter--;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_DECREASE));
        }
        update_all_signals();
    }

    private void button_saveNquit_pressed() {
        System.exit(0);
    }


    @Override
    void set_display() {
        try {
            display_white.setText(spawn_counter);
            display_green.clear();
            display_yellow.clear();


        } catch (IOException e) {
            getLogger().fatal(e);
            System.exit(1);
        }
    }

    @Override
    void set_flag_signals() {
        if (spawn_counter == 0) {
            mySystem.getPinHandler().off(Configs.OUT_FLAG_WHITE);
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:on,1000;off,1000");
            mySystem.getPinHandler().setScheme("rgbflag", "Counter empty", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_RED, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
        } else {
            mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);
            mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,1000;off,1000");
            mySystem.getPinHandler().setScheme("rgbflag", "running", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
        }

        mySystem.getPinHandler().off(Configs.OUT_FLAG_BLUE);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_GREEN);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_YELLOW);
    }

    @Override
    void set_leds_and_buttons() {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,1000;off,1000");
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,1000;off,1000");
        mySystem.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
        mySystem.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);

    }


    @Override
    public String getName() {
        return "Spawncounter";
    }

    @Override
    public boolean isGameRunning() {
        return true;
    }
}
