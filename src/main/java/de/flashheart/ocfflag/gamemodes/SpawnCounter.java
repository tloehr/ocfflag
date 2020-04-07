package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.pinhandler.PinHandler;
import de.flashheart.ocfflag.hardware.pinhandler.RGBScheduleElement;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.awt.*;
import java.io.IOException;

/**
 * In dieser Klasse befindet sich die Spielmechanik.
 */
public class SpawnCounter extends GameMode {

    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getFromConfigs(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);

//    private Display7Segments4Digits display_blue;
//    private Display7Segments4Digits display_red;
//    private Display7Segments4Digits display_white;
//    private Display7Segments4Digits display_green;
//    private Display7Segments4Digits display_yellow;
//
//    private MyAbstractButton button_quit;
//    private MyAbstractButton button_shutdown;
//
//    private MyAbstractButton K1_reset;
//    private MyAbstractButton K2_zero;
//    private MyAbstractButton K3_plus_10;
//    private MyAbstractButton K4_plus_100;
//
//    private MyAbstractButton button_red;
//    private MyAbstractButton button_blue;
//    private MyAbstractButton button_green;
//    private MyAbstractButton button_yellow;
//    private MySystem mySystem;

    private int spawn_counter = 100;
//    private Configs configs;


    public SpawnCounter() {
//        initHardware();
//
//        initGame();
    }

    private void initHardware() {
        configs = (Configs) Main.getFromContext("configs");
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);

        // ApplicationContext
        display_red = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_RED_I2C);
        display_blue = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_BLUE_I2C);
        display_green = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_GREEN_I2C);
        display_yellow = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_YELLOW_I2C);
        display_white = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_WHITE_I2C);

        // GUI Buttons
        button_quit = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_QUIT);
        button_shutdown = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_SHUTDOWN);

        button_red = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1_reset = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_C);  // K1 - stdby actv
        K2_zero = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_A);     // K2 - num teams
        K3_plus_10 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_B);  // K3 - game time
        K4_plus_100 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_D);  // K4 - RESET

        K1_reset.setText("C reset");
        K2_zero.setText("A =0");
        K3_plus_10.setText("B +10");
        K4_plus_100.setText("D +100");

        spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
    }

    private void initGame() {
        button_blue.setReactiontime(0);
        button_blue.setActionListener(e -> {
            getLogger().debug("GUI_button_blue");
            button_action_pressed();
        });
        button_red.setReactiontime(0);
        button_red.setActionListener(e -> {
            getLogger().debug("GUI_button_red");
            button_action_pressed();
        });
        button_green.setReactiontime(0);
        button_green.setActionListener(e -> {
            getLogger().debug("GUI_button_green");
            button_action_pressed();
        });
        button_yellow.setReactiontime(0);
        button_yellow.setActionListener(e -> {
            getLogger().debug("GUI_button_yellow");
            button_action_pressed();
        });
        K2_zero.setActionListener(e -> {
            getLogger().debug("GUI_button_0");
            button_0_pressed();
        });
        K3_plus_10.setActionListener(e -> {
            getLogger().debug("GUI_button_+10");
            button_plus_10_pressed();
        });
        K4_plus_100.setActionListener(e -> {
            getLogger().debug("GUI_button_+100");
            button_plus_100_pressed();
        });
        K1_reset.setActionListener(e -> {
            getLogger().debug("GUI_button_reset");
            buttonResetPressed();
        });
        button_quit.setActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });


        button_shutdown.setActionListener(event -> {
            getLogger().debug("GPIO_button_shutdown DOWN");
            Main.prepareShutdown();
            try {
                String line = Main.getFromConfigs(Configs.SHUTDOWN_COMMAND_LINE);
                CommandLine commandLine = CommandLine.parse(line);
                DefaultExecutor executor = new DefaultExecutor();
                Main.prepareShutdown();
                executor.setExitValue(1);
                executor.execute(commandLine);
//                Thread.sleep(5000);
            } catch (IOException e) {
                getLogger().error(e);
            }
        });

//        mode = MODE_CONFIG;
        setDisplayToEvent();
    }

    private void button_plus_10_pressed() {
//        if (mode == MODE_RUNNING) return;
        spawn_counter += 10;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();
    }

    private void button_plus_100_pressed() {
//        if (mode == MODE_RUNNING) return;
        spawn_counter += 100;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();
    }

    private void button_0_pressed() {
//        if (mode == MODE_RUNNING) return;
        spawn_counter = 0;
        configs.put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();

    }

    private void button_action_pressed() {
//        if (mode != MODE_RUNNING) return;
        if (spawn_counter == 0) return;
        if (spawn_counter == 1) {
            spawn_counter = 0;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_NOMORETICKETS));
        } else {
            spawn_counter--;
            mySystem.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getFromConfigs(Configs.SPWN_SIREN_DECREASE));
        }
        setDisplayToEvent();
    }

    private void button_saveNquit_pressed() {
        System.exit(0);
    }


    private void button_quit_pressed() {
//        if (mode != MODE_CLOCK_PREGAME) return;
        System.exit(0);
    }


    private void buttonResetPressed() {
//        Main.getFrameDebug().addToConfigLog("button_Standby_Active_pressed");
        spawn_counter = configs.getInt(Configs.SPWN_START_TICKETS);
        setDisplayToEvent();
    }


    private void setDisplayToEvent() {
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
