package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.misc.Configs;

import java.awt.*;
import java.io.IOException;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 */
public class SpawnCounter extends GameMode {
    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getFromConfigs(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);
    private int spawn_counter = 100;

    public SpawnCounter() {
        super(1);
    }

    @Override
    void initHardware() {
        super.initHardware();
        k1.setText("C reset/=0");
        k2.setText("Change Game");
        k3.setText("B +10");
        k4.setText("D +100");

        spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
    }

    
    @Override
    void initGame() {
        super.initGame();
        button_quit.setActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });

        setDisplay();
    }

    @Override
    void button_k3_pressed() {
        spawn_counter += 10;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplay();
    }

    @Override
    void button_k4_pressed() {
        spawn_counter += 100;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplay();
    }

    @Override
    void button_k1_pressed() {
        if (spawn_counter == configs.getInt(Configs.SPWN_START_TICKETS)) spawn_counter = 0;
        else spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
        setDisplay();
    }

    @Override
    void button_red_pressed() {
        button_action_pressed();
    }

    @Override
    void button_blue_pressed() {
        button_action_pressed();
    }

    @Override
    void button_green_pressed() {
        button_action_pressed();
    }

    @Override
    void button_yellow_pressed() {
        button_action_pressed();
    }

    private void button_action_pressed() {
        if (spawn_counter == 0) return;
        if (spawn_counter == 1) {
            spawn_counter = 0;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_NOMORETICKETS));
        } else {
            spawn_counter--;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_DECREASE));
        }
        setDisplay();
    }

    private void button_saveNquit_pressed() {
        System.exit(0);
    }


    @Override
     void setDisplay() {
        try {
            display_white.setText(spawn_counter);


//            display_red.setText("CNTR");
//            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,1000;off,1000");
//            display_blue.setText("SPWN");
//            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
            mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,1000;off,1000");


            display_green.clear();
            display_yellow.clear();

//            Main.getPinHandler().off("led_red_button");
//            Main.getPinHandler().off("led_blue_button");
            mySystem.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
            mySystem.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);

            if (spawn_counter == 0) {
                mySystem.getPinHandler().off(Configs.OUT_FLAG_WHITE);
                mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:on,1000;off,1000");
                mySystem.getPinHandler().setScheme("rgbflag", "Counter empty", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_RED, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
            } else {
                mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);
                mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,1000;off,1000");
                mySystem.getPinHandler().setScheme("rgbflag", "running", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
            }
//            Main.getPinHandler().off("flag_white");
//            Main.getPinHandler().off("flag_red");
            mySystem.getPinHandler().off(Configs.OUT_FLAG_BLUE);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_GREEN);
            mySystem.getPinHandler().off(Configs.OUT_FLAG_YELLOW);

        } catch (IOException e) {
            getLogger().fatal(e);
            System.exit(1);
        }
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
