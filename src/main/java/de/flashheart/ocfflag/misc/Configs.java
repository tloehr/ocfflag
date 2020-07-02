package de.flashheart.ocfflag.misc;

import com.pi4j.io.gpio.RaspiPin;
import de.flashheart.ocfflag.Main;
import de.flashheart.ocfflag.interfaces.HasLogger;
import org.apache.log4j.Level;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public class Configs implements HasLogger {
    public static final String THE_CONFIGS = "the_configs";
    public static final String SHUTDOWN_COMMAND_LINE = "shutdown_cmd_line";
    public static final String MY_SYSTEM = "mysystem";
    public static final String IGNORE_GPIO_IN_ARM_MODE = "ignore_gpios";
    public static final String DEV_MODE = "dev_mode";
    public static final String GPIOCONTROLLER = "gpiocontroller";
    public static final String I2CBUS = "i2cbus";
    public static final String MCP23017_1 = "mcp23017_1";

    private final SortedProperties configs;
    private final Properties applicationContext;

    public static final String MATCHID = "matchid";
    public static final String MYUUID = "uuid";
    public static final String LOGLEVEL = "loglevel";

    public static final String REST_URL = "resturl";
    public static final String REST_AUTH = "restauth";


    public static final String BUTTON_REACTION_TIME = "button_reaction_time";

    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String SLEEP_PER_CYCLE = "sleep_per_cycle";

    public static final String SIRENS_ENABLED = "sirens_enabled";

    //    public static final String NUMBER_OF_TEAMS = "num_teams";
    public static final String MAX_NUMBER_OF_TEAMS = "max_teams";

    public static final String FLAG_RGB_WHITE = "flag_rgb_white";
    public static final String FLAG_RGB_BLUE = "flag_rgb_blue";
    public static final String FLAG_RGB_RED = "flag_rgb_red";
    public static final String FLAG_RGB_GREEN = "flag_rgb_green";
    public static final String FLAG_RGB_YELLOW = "flag_rgb_yellow";


    public static final String LCD_HARDWARE = "lcdhardware";
    public static final String LCD_MODEL = "lcdmodel";
    public static final String LCD_I2C_ADDRESS = "lcdi2c";


    public static final String ALPHA_LED1_I2C = "alpha_led1_i2c";
    public static final String ALPHA_LED2_I2C = "alpha_led2_i2c";
    public static final String ALPHA_LED3_I2C = "alpha_led3_i2c";
    public static final String ALPHA_LED4_I2C = "alpha_led4_i2c";
    public static final String ALPHA_LED_MODEL = "alpha_led_mode";

    // Application Context Keys - irgendwo müssen sie ja stehen
//    public static final String AC_BTN_A = "flag_rgb_yellow";


    // Spielmodus-Eigene Einstellungen

    public static final String RLGS_GAMEMODES = "rlgs_gamemodes";


    // OCF
    public static final String OCF_GAMETIME = "ocf_gametime";
    public static final String OCF_COLORCHANGE_SIGNAL = "ocf_colorchange_signal";
    public static final String OCF_START_STOP_SIGNAL = "ocf_start_stop_signal";
    public static final String OCF_GAME_TIME_LIST = "ocf_game_time_list";
    public static final String OCF_TIME_ANNOUNCER = "ocf_time_announcer"; // schaltet das Blinken gemäß der Restspielzeit ein oder aus.


    // SpawnCounter
    public static final String SPWN_START_TICKETS = "spawn_start_tickets";
    public static final String SPWN_SIREN_NOMORETICKETS = "spawn_siren_nomoretickets";
    public static final String SPWN_SIREN_DECREASE = "spawn_siren_decrease";
    public static final String SPWN_START_STOP_SIGNAL = "spawn_start_stop_signal";


    // Allgemeine Einstellungen

    // allgemeine Hardware Einstellungen und Zuordnungen
    public static final String DISPLAY_RED_I2C = "display_red_i2c";
    public static final String DISPLAY_BLUE_I2C = "display_blue_i2c";
    public static final String DISPLAY_YELLOW_I2C = "display_yellow_i2c";
    public static final String DISPLAY_GREEN_I2C = "display_green_i2c";
    public static final String DISPLAY_WHITE_I2C = "display_white_i2c";


    // Display Helligkeiten
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";
    public static final String BRIGHTNESS_YELLOW = "brightness_yellow";
    public static final String BRIGHTNESS_GREEN = "brightness_green";


    // Buttons: Zuordnung zu den GPIOs

    //

    // Config Buttons. Ich mach hier zwei Zuordnungen. Die OCF Flagge benutzt eine Fernsteuerung die von A-D
    // beschriftet ist. Auf der Platine steht K1 - K4
    public static final String BUTTON_A = "button_a"; // num_teams
    public static final String BUTTON_B = "button_b"; // standby_active
    public static final String BUTTON_C = "button_c"; // gametime
    public static final String BUTTON_D = "button_d"; // UNDO / RESET

    public static final String BUTTON_K1 = BUTTON_B; // standby_active
    public static final String BUTTON_K2 = BUTTON_A; // num_teams
    public static final String BUTTON_K3 = BUTTON_C; // gametime
    public static final String BUTTON_K4 = BUTTON_D; // UNDO / RESET

    // System Buttons
    public static final String BUTTON_SHUTDOWN = "button_shutdown";
    public static final String BUTTON_QUIT = "button_quit"; // nur in der GUI, nicht in Hardware

    // Player Buttons
    public static final String BUTTON_RED = "button_red";
    public static final String BUTTON_BLUE = "button_blue";
    public static final String BUTTON_GREEN = "button_green";
    public static final String BUTTON_YELLOW = "button_yellow";


    // und zu den Mosfets
    public static final String OUT_LED_RED_BTN = "ledRedButton";
    public static final String OUT_LED_BLUE_BTN = "ledBlueButton";
    public static final String OUT_LED_GREEN_BTN = "ledGreenButton";
    public static final String OUT_LED_YELLOW_BTN = "ledYellowButton";
    public static final String OUT_LED_GREEN = "ledGreen";
    public static final String OUT_LED_WHITE = "ledWhite";
    public static final String OUT_FLAG_WHITE = "flag_white";
    public static final String OUT_FLAG_RED = "flag_red";
    public static final String OUT_FLAG_BLUE = "flag_blue";
    public static final String OUT_FLAG_GREEN = "flag_green";
    public static final String OUT_FLAG_YELLOW = "flag_yellow";

    public static final String OUT_SIREN_START_STOP = "siren_start_stop"; // für start/stop signal
    public static final String OUT_SIREN_COLOR_CHANGE = "siren_color_change"; // für ereignis anzeige. z.B. Farbwechsel
    public static final String OUT_SIREN_SHUTDOWN = "siren_shutdown"; // für ereignis anzeige. z.B. Farbwechsel
    public static final String OUT_HOLDDOWN_BUZZER = "siren_holddown_buzzer"; // ein einfacher Buzzer

    public static final String FRAME_DEBUG = "FrameDebug";

    public static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = "siren_to_announce_the_color_change";

    // nur für die RGB Flagge als Key für den PinHandler. Habs hier hin gepackt, damit das einheitlich ist.
    public static final String OUT_RGB_FLAG = "rgbflag";
    public static final String RGB_PIN_RED = "rgb_pin_red";
    public static final String RGB_PIN_GREEN = "rgb_pin_green";
    public static final String RGB_PIN_BLUE = "rgb_pin_blue";


    public static HashMap<String, Color> getColors() {
        HashMap<String, Color> colors = new HashMap<>();
        // wird manchmal gebraucht. Z.B. bei der OCFFlagge. Ist nur zur Bequemlichkeit beim Programmieren.
        colors.put("green", Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_GREEN)));
        colors.put("red", Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_RED)));
        colors.put("blue", Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_BLUE)));
        colors.put("yellow", Tools.getColor(Main.getFromConfigs(Configs.FLAG_RGB_YELLOW)));
        return colors;
    }

    public Configs() throws IOException {
        configs = new SortedProperties(); // Einstellungen, die verändert werden
        applicationContext = new Properties(); // inhalte der application.properties (von Maven)

        // defaults
        configs.put(RLGS_GAMEMODES, "OCF2,OCF3,OCF4,SPWN");
        configs.put(MATCHID, "1");
        configs.put(SLEEP_PER_CYCLE, "500");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "RLG #" + new java.util.Random().nextInt());
        configs.put(BUTTON_REACTION_TIME, "0");

        // Hardware Defaults
        // Buttons benutzen immer den Raspi GPIO Provider
        configs.put(BUTTON_A, "GPIO 12");
        configs.put(BUTTON_B, "GPIO 3");
        configs.put(BUTTON_C, "GPIO 13");
        configs.put(BUTTON_D, "GPIO 14");

        configs.put(BUTTON_RED, RaspiPin.GPIO_21.getName());
        configs.put(BUTTON_BLUE, "GPIO 22");
        configs.put(BUTTON_GREEN, "GPIO 23");
        configs.put(BUTTON_YELLOW, "GPIO 24");
        configs.put(BUTTON_SHUTDOWN, "GPIO 28"); // Bei RASPI2 muss es der GPIO25 sein, beim RASPI3 der GPIO28. Sehr seltsam.

        // Alle anderen den MCP23017

        configs.put(OUT_LED_RED_BTN, "mf01");
        configs.put(OUT_LED_BLUE_BTN, "mf02");
        configs.put(OUT_LED_GREEN_BTN, "mf04");
        configs.put(OUT_LED_YELLOW_BTN, "mf05");
        configs.put(OUT_LED_GREEN, "mf03");
        configs.put(OUT_LED_WHITE, "mf06");


        configs.put(OUT_FLAG_WHITE, "mf08");
        configs.put(OUT_FLAG_RED, "mf09"); // actioncase mf09  // unused in ocfflag
        configs.put(OUT_FLAG_BLUE, "mf10"); // actioncase mf10   // unused in ocfflag
        configs.put(OUT_FLAG_GREEN, "mf11");
        configs.put(OUT_FLAG_YELLOW, "mf12");

        configs.put(OUT_SIREN_COLOR_CHANGE, "rly01"); // ocfflag2 mf10  // rly02
        configs.put(OUT_SIREN_SHUTDOWN, "rly02"); // ocfflag2 mf10  // rly02
        configs.put(OUT_SIREN_START_STOP, "rly03"); //mf09  // orig: rly01
        configs.put(OUT_HOLDDOWN_BUZZER, "mf15");
        configs.put(SIRENS_ENABLED, "true");

        configs.put(LCD_I2C_ADDRESS, "0x27");

//        // RESERVE
//        configs.put(OUT_MF07, "mf07");
//        configs.put(OUT_MF13, "mf13");
//        configs.put(OUT_MF14, "mf14");
//        configs.put(OUT_MF16, "mf16");

        configs.put(REST_URL, "http://localhost:8090/rest/gamestate/create");
        configs.put(REST_AUTH, "Torsten:test1234");

        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_GREEN, "10");
        configs.put(BRIGHTNESS_YELLOW, "10");
        configs.put(MIN_STAT_SEND_TIME, "0"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht


//        configs.put(OCF_GAME_TIME_LIST, "600000,900000,1200000,1800000,3600000,5400000,7200000,9000000,10800000,12600000,14400000,16200000,17999000");

        configs.put(DISPLAY_RED_I2C, "0x72");
        configs.put(DISPLAY_BLUE_I2C, "0x71");
        configs.put(DISPLAY_YELLOW_I2C, "0x73");
        configs.put(DISPLAY_GREEN_I2C, "0x74");

        configs.put(ALPHA_LED1_I2C, "0x70");
        configs.put(ALPHA_LED2_I2C, "0x75");
        configs.put(ALPHA_LED3_I2C, "0x76");
        configs.put(ALPHA_LED4_I2C, "0x77");

        // deprecated - rgb will be removed in future releases
        // colors for the rgb leds
        configs.put(FLAG_RGB_WHITE, "white");
        configs.put(FLAG_RGB_BLUE, "blue");
        configs.put(FLAG_RGB_GREEN, "green");
        configs.put(FLAG_RGB_RED, "red");
        configs.put(FLAG_RGB_YELLOW, "#ff8000");


        // OCF
        configs.put(OCF_GAMETIME, "0");
        configs.put(OCF_START_STOP_SIGNAL, "1:on,5000;off,1");
        configs.put(OCF_COLORCHANGE_SIGNAL, "2:on,50;off,50");
        configs.put(OCF_GAME_TIME_LIST, "1,10,15,20,30,45,60,75,90,105,120");
        configs.put(OCF_TIME_ANNOUNCER, "true");

        configs.put(SPWN_SIREN_DECREASE, "2:on,50;off,50");
        configs.put(SPWN_SIREN_NOMORETICKETS, "1:on,2000;off,1");
        configs.put(SPWN_START_TICKETS, "100");


        //todo: mit ins Installationspaket
        /**
         * shutdown.sh
         * #! /bin/bash
         * kill -15 `pidof java`
         * sleep 5s
         * shutdown -h now
         */
        configs.put(SHUTDOWN_COMMAND_LINE, "sudo nohup /bin/sh /home/pi/ocfflag/shutdown.sh &");

        configs.put(SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE, OUT_SIREN_START_STOP); // to override the use of the color change siren. if we only have ONE siren to signal both events

        // configdatei einlesen
        loadConfigs();
        loadApplicationContext();

        // und der Rest
        getLogger().setLevel(Level.toLevel(configs.getProperty(LOGLEVEL), Level.DEBUG));
        if (!configs.containsKey(MYUUID)) {
            configs.put(MYUUID, UUID.randomUUID().toString());
        }

    }


    public long getNextMatchID() {
        long nextmachtid = Long.parseLong(get(MATCHID)) + 1l;
        put(MATCHID, Long.toString(nextmachtid));
        return nextmachtid;
    }

    private void loadApplicationContext() throws IOException {
        InputStream in2 = Main.class.getResourceAsStream("/application.properties");
        applicationContext.load(in2);
        in2.close();
    }

    private void loadConfigs() throws IOException {
        File configFile = new File(Tools.getWorkingPath() + File.separator + "config.txt");
        configFile.getParentFile().mkdirs();
        configFile.createNewFile(); // falls nicht vorhanden

        FileInputStream in = new FileInputStream(configFile);
        Properties p = new SortedProperties();
        p.load(in);
        configs.putAll(p);
        p.clear();
        in.close();
    }

    public void put(Object key, Object value) {
        configs.put(key, value.toString());
        saveConfigs();
    }

    public String getApplicationInfo(Object key) {
        return applicationContext.containsKey(key) ? applicationContext.get(key).toString() : "null";
    }

    public boolean is(Object key) {
        return Boolean.parseBoolean(configs.containsKey(key) ? configs.get(key).toString() : "false");
    }

    public int getInt(Object key) {
        return Integer.parseInt(configs.containsKey(key) ? configs.get(key).toString() : "-1");
    }

    public long getLong(Object key) {
        return Long.parseLong(configs.containsKey(key) ? configs.get(key).toString() : "-1");
    }

    public String get(Object key) {
        return configs.containsKey(key) ? configs.get(key).toString() : "null";
    }

    private void saveConfigs() {
        try {
            File configFile = new File(Tools.getWorkingPath() + File.separator + "config.txt");
            FileOutputStream out = new FileOutputStream(configFile);
            configs.store(out, "Settings OCFFlag");
            out.close();
        } catch (Exception ex) {
            getLogger().fatal(ex);
            System.exit(1);
        }
    }

    public Long[] getGameTimes() {
        String[] listTimes = get(OCF_GAME_TIME_LIST).split("\\,");
        ArrayList<Long> list = new ArrayList<>();
        for (String time : listTimes) list.add(Long.parseLong(time));
        return list.toArray(new Long[list.size()]);
    }


}
