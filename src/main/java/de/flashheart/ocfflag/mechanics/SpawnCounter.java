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
public class SpawnCounter extends Games implements HasLogger {

    private static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = Main.getConfigs().get(Configs.SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE);
//    private final int MODE_CONFIG = 0;
//    private final int MODE_RUNNING = 1;


    private int spawn_counter = 100;


    public SpawnCounter() {
        super();
        K1.setText("C reset");
        K2.setText("A =0");
        K3.setText("B +10");
        K4.setText("D +100");

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
        K2.addActionListener(e -> {
            getLogger().debug("GUI_button_0");
            button_0_pressed();
        });
        K3.addActionListener(e -> {
            getLogger().debug("GUI_button_+10");
            button_plus_10_pressed();
        });
        K4.addActionListener(e -> {
            getLogger().debug("GUI_button_+100");
            button_plus_100_pressed();
        });
        K1.addActionListener(e -> {
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

    @Override
    public String getShortID() {
        return "SC";
    }
}
