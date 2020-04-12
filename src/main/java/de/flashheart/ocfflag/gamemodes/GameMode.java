package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.hardware.abstraction.Display7Segments4Digits;
import de.flashheart.ocfflag.hardware.abstraction.MyAbstractButton;
import de.flashheart.ocfflag.hardware.sevensegdisplay.LEDBackPack;
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
    int num_teams;

    long matchlength, matchtime, remaining;

    long SLEEP_PER_CYCLE = 500;

    GameMode() {
        this(2);
    }

    GameMode(int num_teams) {
        this.num_teams = num_teams;
        init();
        initGame();
    }

    GameMode(int num_teams, long matchlength) {
        init();
        this.num_teams = num_teams;
        this.matchlength = matchlength;
        initGame();
    }

    private void init() {
        configs = (Configs) Main.getFromContext("configs");
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);
        initHardware();
        matchlength = 0l;
        matchtime = 0l;
        remaining = 0l;
    }

    public abstract String getName();

    public abstract boolean isGameRunning();

    public void start_gamemode() {
        getLogger().debug("\n\n==================================================");
        getLogger().debug("starting gamemode: " + getName());
    }

    public void stop_gamemode() {
        getLogger().debug("stopping gamemode: " + getName());
        getLogger().debug("==================================================\n\n");
//        getLogger().debug("                                                  ");
        mySystem.getPinHandler().off();
    }

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
        getLogger().debug("button_red_pressed");
    }

    void button_blue_pressed() {
        getLogger().debug("button_blue_pressed");
    }

    void button_green_pressed() {
        getLogger().debug("button_green_pressed");
    }

    void button_yellow_pressed() {
        getLogger().debug("button_yellow_pressed");
    }

    void button_k4_pressed() {
        getLogger().debug("button_k4_pressed: " + k4.getText());
    }

    void button_k1_pressed() {
        getLogger().debug("button_k4_pressed: " + k1.getText());
    }

    void button_k3_pressed() {
        getLogger().debug("button_k4_pressed: " + k3.getText());
    }

    void set_blinking_red_button(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_RED_BTN, scheme);
    }

    void set_blinking_blue_button(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_BLUE_BTN, scheme);
    }

    void set_blinking_green_button(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN_BTN, scheme);
    }

    void set_blinking_yellow_button(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_YELLOW_BTN, scheme);
    }

    void set_blinking_flag_rgb(String scheme) {
        set_blinking_flag_rgb(null, scheme);
    }

    void set_blinking_flag_white(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_WHITE, scheme);
    }

    void set_blinking_flag_red(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_RED, scheme);
    }

    void set_blinking_flag_blue(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_BLUE, scheme);
    }

    void set_blinking_flag_green(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_GREEN, scheme);
    }

    void set_blinking_flag_yellow(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_FLAG_YELLOW, scheme);
    }

    void set_blinking_led_white(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_WHITE, scheme);
    }

    void set_blinking_led_green(String scheme) {
        mySystem.getPinHandler().setScheme(Configs.OUT_LED_GREEN, scheme);
    }

    void off_red_button() {
        set_blinking_red_button("0:");
    }

    void off_blue_button() {
        set_blinking_blue_button("0:");
    }

    void off_green_button() {
        set_blinking_green_button("0:");
    }

    void off_yellow_button() {
        set_blinking_yellow_button("0:");
    }

    void off_flag_white() {
        set_blinking_flag_white("0:");
    }

    void off_flag_red() {
        set_blinking_flag_red("0:");
    }

    void off_flag_blue() {
        set_blinking_flag_blue("0:");
    }

    void off_flag_green() {
        set_blinking_flag_green("0:");
    }

    void off_flag_yellow() {
        set_blinking_flag_yellow("0:");
    }

    void set_blinking_flag_rgb(String text, String scheme) {
        if (text != null) mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, text, scheme);
        else mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, scheme);
    }

    void blinking_off() {
        try {
            display_white.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_red.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_blue.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_green.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
            display_yellow.setBlinkRate(LEDBackPack.HT16K33_BLINKRATE_OFF);
        } catch (IOException e) {
            getLogger().error(e);
        }
        mySystem.getPinHandler().off(Configs.OUT_LED_RED_BTN);
        mySystem.getPinHandler().off(Configs.OUT_LED_BLUE_BTN);
        mySystem.getPinHandler().off(Configs.OUT_LED_GREEN_BTN);
        mySystem.getPinHandler().off(Configs.OUT_LED_YELLOW_BTN);

        mySystem.getPinHandler().off(Configs.OUT_FLAG_WHITE);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_BLUE);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_GREEN);
        mySystem.getPinHandler().off(Configs.OUT_FLAG_YELLOW);

        mySystem.getPinHandler().off(Configs.OUT_FLAG_RED);

        mySystem.getPinHandler().off(Configs.OUT_LED_GREEN);
        mySystem.getPinHandler().off(Configs.OUT_LED_WHITE);
    }

    void change_game() {
        getLogger().debug("changing game");
        stop_gamemode();
        Main.setGame(new GameSelector());
    }

    void set_siren_scheme(String siren_key, String siren_scheme) {
        siren_key = Main.getFromConfigs(siren_key).equals("null") ? siren_key : Main.getFromConfigs(siren_scheme);
        mySystem.getPinHandler().setScheme(siren_key, siren_key);
    }

    void updateTimers() {
        matchtime = matchlength - remaining;
    }

    abstract void setDisplay();

}
