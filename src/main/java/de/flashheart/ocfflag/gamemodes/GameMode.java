package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.HasLogger;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.io.IOException;

public abstract class GameMode implements HasLogger {

    Display7Segments4Digits display_blue;
    Display7Segments4Digits display_red;
    Display7Segments4Digits display_white;
    Display7Segments4Digits display_green;
    Display7Segments4Digits display_yellow;

    MyAbstractButton button_quit;
    MyAbstractButton button_shutdown;

    MyAbstractButton button_blue;
    MyAbstractButton button_red;
    MyAbstractButton button_green;
    MyAbstractButton button_yellow;

    MyAbstractButton k1;
    MyAbstractButton k2;
    MyAbstractButton k3;
    MyAbstractButton k4;

    // So wie es auf der Platine steht. K1..K4
    String[] K_LABEL = new String[]{"dummy_for_0", "stdby act", "num teams", "game time", "reset"};

    Configs configs;
    MySystem mySystem;

    GameMode() {
        configs = (Configs) Main.getFromContext("configs");
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);
        initHardware();
        initGame();
    }

    public abstract String getName();

    public abstract boolean isGameRunning();

    public abstract void run_game();

    void initHardware() {
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

        button_red.setReactiontime(0);
        button_blue.setReactiontime(0);
        button_green.setReactiontime(0);
        button_yellow.setReactiontime(0);

        // Hardware / GUI Buttons
        k1 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K1);
        k2 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K2);
        k3 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K3);
        k4 = (MyAbstractButton) Main.getFromContext(Configs.BUTTON_K4);

        k1.setText(K_LABEL[1]);
        k2.setText(K_LABEL[2]);
        k3.setText(K_LABEL[3]);
        k4.setText(K_LABEL[4]);

    }

    void initGame() {
        button_blue.setActionListener(e -> {
            getLogger().debug("GUI_button_blue");
            button_blue_pressed();
        });
        button_red.setActionListener(e -> {
            getLogger().debug("GUI_button_red");
            button_red_pressed();
        });
        button_yellow.setActionListener(e -> {
            getLogger().debug("GUI_button_yellow");
            button_yellow_pressed();
        });
        button_green.setActionListener(e -> {
            getLogger().debug("GUI_button_green");
            button_green_pressed();
        });
        k1.setActionListener(e -> {
            getLogger().debug("K1");
            button_k1_pressed();
        });
        k2.setActionListener(e -> {
            getLogger().debug("K2");
            change_game();
        });
        k3.setActionListener(e -> {
            getLogger().debug("K3");
            button_k3_pressed();
        });
        k4.setActionListener(e -> {
            getLogger().debug("K4");
            button_k4_pressed();
        });
        button_quit.setActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });
        button_shutdown.setActionListener(e -> {
            getLogger().debug("GPIO_button_shutdown DOWN");
            Main.prepareShutdown();
            try {
                String line = Main.getFromConfigs(Configs.SHUTDOWN_COMMAND_LINE);
                CommandLine commandLine = CommandLine.parse(line);
                DefaultExecutor executor = new DefaultExecutor();
                Main.prepareShutdown();
                executor.setExitValue(1);
                executor.execute(commandLine);
            } catch (IOException exc) {
                getLogger().error(exc);
            }
        });
    }

    void button_quit_pressed() {
        Main.prepareShutdown();
        System.exit(0);
    }

    void button_red_pressed() {
    }

    void button_blue_pressed() {
    }

    void button_green_pressed() {
    }

    void button_yellow_pressed() {
    }

    void button_k4_pressed() {
    }

    void button_k1_pressed() {
    }

    void button_k3_pressed() {
    }

    void change_game() {
        mySystem.getPinHandler().off();
        Main.setGame(new GameSelector());
    }

}
