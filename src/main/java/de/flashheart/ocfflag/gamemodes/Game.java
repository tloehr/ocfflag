package de.flashheart.ocfflag.gamemodes;

import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.gui.Display7Segments4Digits;
import de.flashheart.ocfflag.gui.LCDTextDisplay;
import de.flashheart.ocfflag.gui.LEDTextDisplay;
import de.flashheart.ocfflag.hardware.HT16K33;
import de.flashheart.ocfflag.hardware.MyAbstractButton;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.interfaces.HasLogger;
import de.flashheart.ocfflag.misc.Configs;

public abstract class Game implements HasLogger {
    public static final String RESUMED = "RESUMED";
    public static final String FUSED = "FUSED";
    public static final String DEFUSED = "DEFUSED";
    public static final String START_GAME = "START_GAME";
    public static final String EXPLODED = "EXPLODED";
    public static final String DEFENDED = "DEFENDED";
    public static final String PREGAME = "PREGAME";
    public static final String GOING_TO_PAUSE = "GNGPAUSE";
    public static final String PAUSING = "PAUSING";
    public static final String GOING_TO_RESUME = "GNGRESUM";
    public static final String GAME_ABORTED = "GAME_ABORTED";
    public static final String LAST_EVENT_REVERTED = "LAST_EVENT_REVERTED";
    public static final String FLAG_NEUTRAL = "FLAG_NEUTRAL";
    public static final String BLUE_ACTIVATED = "BLUE_ACTIVATED";
    public static final String RED_ACTIVATED = "RED_ACTIVATED";
    public static final String YELLOW_ACTIVATED = "YELLOW_ACTIVATED";
    public static final String GREEN_ACTIVATED = "GREEN_ACTIVATED";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String RESULT_RED_WON = "RESULT_RED_WON";
    public static final String RESULT_BLUE_WON = "RESULT_BLUE_WON";
    public static final String RESULT_DRAW = "RESULT_DRAW";
    public static final String RESULT_GREEN_WON = "RESULT_GREEN_WON";
    public static final String RESULT_YELLOW_WON = "RESULT_YELLOW_WON";
    public static final String RESULT_MULTI_WINNERS = "RESULT_MULTI_WINNERS";
    public static final String[] GAME_OVER_EVENTS = new String[]{"GAME_ABORTED", "GAME_OVER", "EXPLODED", "DEFENDED"};

    int SETUP_BUTTON_PAGE;

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

    LEDTextDisplay ledTextDisplay;
    LCDTextDisplay lcdTextDisplay;

    MyAbstractButton k1;
    MyAbstractButton k2;
    MyAbstractButton k3;
    MyAbstractButton k4;

    // So wie es auf der Platine steht. K1..K4
//    String[] K_LABEL = new String[]{"dummy_for_index_0_never_used", "K1", "K2", "K3", "K4"};

    Configs configs;
    MySystem mySystem;
    int num_teams;

    String flag_state;
    int game_state;

    /**
     * Diese Klasse stellt die Grundlage für alle Spielvarianten des Systems dar.
     */
    Game() {
        this(2);
    }

    Game(int num_teams) {
        this.num_teams = num_teams;
        configs = (Configs) Main.getFromContext(Configs.THE_CONFIGS);
        mySystem = (MySystem) Main.getFromContext(Configs.MY_SYSTEM);
    }


    /**
     * hier werden alle Event-Listeners verknüpft.
     */
    void initSoftware() {
        lcdTextDisplay.reset_display();
        SETUP_BUTTON_PAGE = lcdTextDisplay.add_page();
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
            button_k1_pressed();
        });
        k2.setActionListener(e -> {
            button_k2_pressed();
        });
        k3.setActionListener(e -> {
            button_k3_pressed();
        });
        k4.setActionListener(e -> {
            button_k4_pressed();
        });
        button_quit.setActionListener(e -> {
            getLogger().debug("GUI_button_quit");
            button_quit_pressed();
        });
        button_shutdown.setActionListener(e -> {
            getLogger().debug("SHUTDOWN SYSTEM");
            shutdown_system();
        });
    }

    public abstract String getName();

    public abstract boolean isGameRunning();


    /**
     * damit wird der eigentliche Game-Mode gestartet
     */
    public void start_gamemode() {
        getLogger().debug("\n\n==================================================");
        getLogger().debug("starting gamemode: " + getName());
        initHardware();
        initSoftware();
        initGame();
    }

    /**
     * kurz bevor der Game-Mode endet, wir diese Methode aufgerufen.
     * Meistens vor einem Game-Mode wechsel.
     */
    public void stop_gamemode() {
        getLogger().debug("stopping gamemode: " + getName());
        getLogger().debug("==================================================\n\n");
        mySystem.getPinHandler().off();
    }

    /**
     * initialisiert sämtliche Hardware-Komponenten
     */
    void initHardware() {

        display_red = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_RED_I2C);
        display_blue = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_BLUE_I2C);
        display_green = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_GREEN_I2C);
        display_yellow = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_YELLOW_I2C);
        display_white = (Display7Segments4Digits) Main.getFromContext(Configs.DISPLAY_WHITE_I2C);

        ledTextDisplay = (LEDTextDisplay) Main.getFromContext(Configs.LED_TEXT_DISPLAY);
        lcdTextDisplay = (LCDTextDisplay) Main.getFromContext(Configs.LCD_TEXT_DISPLAY);

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

        set_config_buttons_labels("K1", "K2", "K3", "K4");

    }

    protected void set_config_buttons_labels(String k1text, String k2text, String k3text, String k4text) {
        k1.setText(k1text);
        k2.setText(k2text);
        k3.setText(k3text);
        k4.setText(k4text);

        // Schreibe die neue Belegung der Config-Tasten auf das LCD Display
//        int lcdpage_for_config_buttons = Integer.parseInt(Main.getFromContext(Configs.LCDPAGE_FOR_CONFIG_BUTTONS).toString());
//        ((LCDTextDisplay) Main.getFromContext(Configs.LCD_TEXT_DISPLAY)).update_page(lcdpage_for_config_buttons, k1text, k2text, k3text, k4text);

        lcdTextDisplay.update_page(SETUP_BUTTON_PAGE, k1text, k2text, k3text, k4text);


    }

    void shutdown_system() {
        ((MySystem) Main.getFromContext(Configs.MY_SYSTEM)).shutdown();
    }

    /**
     * wird von den eigentlichen Klassen implementiert um alle GameMode bezogenen Initialisierungen durchzuführen.
     */

    abstract void initGame();

    void button_quit_pressed() {
        shutdown_system();
        System.exit(0);
    }

    void button_red_pressed() {
        getLogger().debug("button_red_pressed");
        button_teamcolor_pressed(RED_ACTIVATED);
    }

    void button_teamcolor_pressed(String FLAGSTATE) {
    }

    void button_blue_pressed() {
        getLogger().debug("button_blue_pressed");
        button_teamcolor_pressed(BLUE_ACTIVATED);
    }

    void button_green_pressed() {
        getLogger().debug("button_green_pressed");
        button_teamcolor_pressed(GREEN_ACTIVATED);
    }

    void button_yellow_pressed() {
        getLogger().debug("button_yellow_pressed");
        button_teamcolor_pressed(YELLOW_ACTIVATED);
    }

    void button_k4_pressed() {
        getLogger().debug("button_k4_pressed: " + k4.getText());
        getLogger().debug("default: CHANGE GAME");
        stop_gamemode();
        Main.setGame(new GameSelector());
    }

    void button_k1_pressed() {
        getLogger().debug("button_k1_pressed: " + k1.getText());
    }

    void button_k2_pressed() {
        getLogger().debug("button_k2_pressed: " + k2.getText());
    }

    void button_k3_pressed() {
        getLogger().debug("button_k3_pressed: " + k3.getText());
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

    void off_white_led() {
        set_blinking_led_white("0:");
    }

    void off_green_led() {
        set_blinking_led_green("0:");
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

    void off_white_flag() {
        set_blinking_flag_white("0:");
    }

    void off_red_flag() {
        set_blinking_flag_red("0:");
    }

    void off_blue_flag() {
        set_blinking_flag_blue("0:");
    }

    void off_rgb_flag() {
        set_blinking_flag_rgb("0:");
    }

    void off_green_flag() {
        set_blinking_flag_green("0:");
    }

    void off_yellow_flag() {
        set_blinking_flag_yellow("0:");
    }

    void set_blinking_flag_rgb(String text, String scheme) {
        if (text != null) mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, text, scheme);
        else mySystem.getPinHandler().setScheme(Configs.OUT_RGB_FLAG, scheme);
    }


    void set_siren_scheme(String siren_key, String siren_scheme) {

        mySystem.getPinHandler().setScheme(siren_key, Main.getFromConfigs(siren_scheme));
    }

    abstract void setDisplay();

    abstract void setFlagSignals();

    abstract void setLEDsAndButtons();

    void update_all_signals() {
        all_off();
        setDisplay();
        setLEDsAndButtons();
        setFlagSignals();
    }

    void all_off() {

        display_white.setBlinkRate(HT16K33.HT16K33_BLINKRATE_OFF);
        display_red.setBlinkRate(HT16K33.HT16K33_BLINKRATE_OFF);
        display_blue.setBlinkRate(HT16K33.HT16K33_BLINKRATE_OFF);
        display_green.setBlinkRate(HT16K33.HT16K33_BLINKRATE_OFF);
        display_yellow.setBlinkRate(HT16K33.HT16K33_BLINKRATE_OFF);

        off_blue_button();
        off_red_button();
        off_green_button();
        off_yellow_button();

        off_white_flag();
        off_red_flag();
        off_blue_flag();
        off_green_flag();
        off_yellow_flag();

        off_rgb_flag();

        off_white_led();
        off_green_led();

    }

}
