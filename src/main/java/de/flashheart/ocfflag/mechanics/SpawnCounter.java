package de.flashheart.ocfflag.mechanics;

import de.flashheart.ocfflag.Main;
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
public class SpawnCounter implements HasLogger, Games {

    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getConfigs().get(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);

    private final Display7Segments4Digits display_blue;
    private final Display7Segments4Digits display_red;
    private final Display7Segments4Digits display_white;
    private final Display7Segments4Digits display_green;
    private final Display7Segments4Digits display_yellow;

    private final MyAbstractButton button_quit;
    private final MyAbstractButton button_shutdown;

    private final MyAbstractButton K1_reset;
    private final MyAbstractButton K2_zero;
    private final MyAbstractButton K3_plus_10;
    private final MyAbstractButton K4_plus_100;

    private final MyAbstractButton button_red;
    private final MyAbstractButton button_blue;
    private final MyAbstractButton button_green;
    private final MyAbstractButton button_yellow;

    private int spawn_counter = 100;


    public SpawnCounter() {

        // ApplicationContext
        display_red = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_RED_I2C);
        display_blue = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_BLUE_I2C);
        display_green = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_GREEN_I2C);
        display_yellow = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_YELLOW_I2C);
        display_white = (Display7Segments4Digits) Main.getApplicationContext().get(Configs.DISPLAY_WHITE_I2C);

        // GUI Buttons
        button_quit = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_QUIT);
        button_shutdown = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_SHUTDOWN);

        button_red = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_RED);
        button_blue = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_BLUE);
        button_green = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_GREEN);
        button_yellow = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_YELLOW);

        // Hardware / GUI Buttons
        K1_reset = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_C);  // K1 - stdby actv
        K2_zero = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_A);     // K2 - num teams
        K3_plus_10 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_B);  // K3 - game time
        K4_plus_100 = (MyAbstractButton) Main.getApplicationContext().get(Configs.BUTTON_D);  // K4 - RESET

        K1_reset.setText("C reset");
        K2_zero.setText("A =0");
        K3_plus_10.setText("B +10");
        K4_plus_100.setText("D +100");

        spawn_counter = Main.getConfigs().getInt(Configs.SPWN_START_TICKETS);

        initGame();
    }

    private void initGame() {
        button_blue.setReactiontime(0);
        button_blue.addActionListener(e -> {
            getLogger().debug("GUI_button_blue");
            button_action_pressed();
        });
        button_red.setReactiontime(0);
        button_red.addActionListener(e -> {
            getLogger().debug("GUI_button_red");
            button_action_pressed();
        });
        button_green.setReactiontime(0);
        button_green.addActionListener(e -> {
            getLogger().debug("GUI_button_green");
            button_action_pressed();
        });
        button_yellow.setReactiontime(0);
        button_yellow.addActionListener(e -> {
            getLogger().debug("GUI_button_yellow");
            button_action_pressed();
        });
        K2_zero.addActionListener(e -> {
            getLogger().debug("GUI_button_0");
            button_0_pressed();
        });
        K3_plus_10.addActionListener(e -> {
            getLogger().debug("GUI_button_+10");
            button_plus_10_pressed();
        });
        K4_plus_100.addActionListener(e -> {
            getLogger().debug("GUI_button_+100");
            button_plus_100_pressed();
        });
        K1_reset.addActionListener(e -> {
            getLogger().debug("GUI_button_reset");
            buttonResetPressed();
        });
        button_quit.addActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });


        button_shutdown.addActionListener(event -> {
            getLogger().debug("GPIO_button_shutdown DOWN");
            Main.prepareShutdown();
            try {
                String line = Main.getConfigs().get(Configs.SHUTDOWN_COMMAND_LINE);
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
        Main.getConfigs().put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();
    }

    private void button_plus_100_pressed() {
//        if (mode == MODE_RUNNING) return;
        spawn_counter += 100;
        Main.getConfigs().put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();
    }

    private void button_0_pressed() {
//        if (mode == MODE_RUNNING) return;
        spawn_counter = 0;
        Main.getConfigs().put(Configs.SPWN_START_TICKETS, spawn_counter);
        setDisplayToEvent();

    }

    private void button_action_pressed() {
//        if (mode != MODE_RUNNING) return;
        if (spawn_counter == 0) return;
        if (spawn_counter == 1) {
            spawn_counter = 0;
            Main.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getConfigs().get(Configs.SPWN_SIREN_NOMORETICKETS));
        } else {
            spawn_counter--;
            Main.getPinHandler().setScheme(Configs.OUT_HOLDDOWN_BUZZER, Main.getConfigs().get(Configs.SPWN_SIREN_DECREASE));
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
        spawn_counter = Main.getConfigs().getInt(Configs.SPWN_START_TICKETS);
        setDisplayToEvent();
    }


    private void setDisplayToEvent() {
        try {
            display_white.setText(spawn_counter);


//            display_red.setText("CNTR");
//            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
            Main.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, "∞:on,1000;off,1000");
//            display_blue.setText("SPWN");
//            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_1HZ);
            Main.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, "∞:on,1000;off,1000");


            display_green.clear();
            display_yellow.clear();

//            Main.getPinHandler().off("led_red_button");
//            Main.getPinHandler().off("led_blue_button");
            Main.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
            Main.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);

            if (spawn_counter == 0) {
                Main.getPinHandler().off(Configs.OUT_FLAG_WHITE);
                Main.getPinHandler().setScheme(Configs.OUT_FLAG_RED, "∞:on,1000;off,1000");
                Main.getPinHandler().setScheme("rgbflag", "Counter empty", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_RED, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
            } else {
                Main.getPinHandler().off(Configs.OUT_FLAG_RED);
                Main.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, "∞:on,1000;off,1000");
                Main.getPinHandler().setScheme("rgbflag", "running", PinHandler.FOREVER + ":" + new RGBScheduleElement(Configs.FLAG_RGB_WHITE, 1000l) + ";" + new RGBScheduleElement(Color.BLACK, 1000l));
            }
//            Main.getPinHandler().off("flag_white");
//            Main.getPinHandler().off("flag_red");
            Main.getPinHandler().off(Configs.OUT_FLAG_BLUE);
            Main.getPinHandler().off(Configs.OUT_FLAG_GREEN);
            Main.getPinHandler().off(Configs.OUT_FLAG_YELLOW);

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
