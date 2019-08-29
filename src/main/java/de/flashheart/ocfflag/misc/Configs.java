package de.flashheart.ocfflag.misc;

import com.pi4j.io.gpio.Pin;
import de.flashheart.ocfflag.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

public class Configs {
    public static final String SHUTDOWN_COMMAND_LINE ="shutdown_cmd_line";
    private final SortedProperties configs;
    private final Properties applicationContext;
    private final Logger logger = Logger.getLogger(getClass());


    public static final String MATCHID = "matchid";
    public static final String MYUUID = "uuid";
    public static final String LOGLEVEL = "loglevel";

    public static final String REST_URL = "resturl";
    public static final String REST_AUTH = "restauth";

    public static final String DISPLAY_RED_I2C = "display_red_i2c";
    public static final String DISPLAY_BLUE_I2C = "display_blue_i2c";
    public static final String DISPLAY_YELLOW_I2C = "display_yellow_i2c";
    public static final String DISPLAY_GREEN_I2C = "display_green_i2c";
    public static final String DISPLAY_WHITE_I2C = "display_white_i2c";
    
    public static final String BUTTON_REACTION_TIME = "button_reaction_time";

    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String GAMETIME = "gametime";
    public static final String SLEEP_PER_CYCLE = "sleep_per_cycle";
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";
    public static final String BRIGHTNESS_YELLOW = "brightness_yellow";
    public static final String BRIGHTNESS_GREEN = "brightness_green";
    public static final String AIRSIREN_SIGNAL = "airsiren_signal";
    public static final String SIRENS_ENABLED = "sirens_enabled";
    public static final String COLORCHANGE_SIREN_SIGNAL = "colorchange_siren_signal";
    public static final String GAME_TIME_LIST = "game_time_list";

    public static final String NUMBER_OF_TEAMS = "num_teams";
    public static final String MAX_NUMBER_OF_TEAMS = "max_teams";

    public static final String TIME_ANNOUNCER = "time_announcer"; // schaltet das Blinken gemäß der Restspielzeit ein oder aus.

    public static final String FLAG_RGB_WHITE = "flag_rgb_white";
    public static final String FLAG_RGB_BLUE = "flag_rgb_blue";
    public static final String FLAG_RGB_RED = "flag_rgb_red";
    public static final String FLAG_RGB_GREEN = "flag_rgb_green";
    public static final String FLAG_RGB_YELLOW = "flag_rgb_yellow";


    // Buttons: Zuordnung zu den GPIOs
    public static final String BUTTON_STANDBY_ACTIVE = "button_standby_active";
    public static final String BUTTON_PRESET_NUM_TEAMS = "button_preset_num_teams";
    public static final String BUTTON_PRESET_GAMETIME = "button_preset_gametime";
    public static final String BUTTON_RESET = "button_reset";
    public static final String BUTTON_RED = "button_red";
    public static final String BUTTON_BLUE = "button_blue";
    public static final String BUTTON_GREEN = "button_green";
    public static final String BUTTON_YELLOW = "button_yellow";
    public static final String BUTTON_SHUTDOWN = "button_shutdown";

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

    public static final String OUT_SIREN_START_STOP = "siren_start_stop";
    public static final String OUT_SIREN_COLOR_CHANGE = "siren_color_change";
    public static final String OUT_HOLDDOWN_BUZZER  = "siren_holddown_buzzer";

    // Test reasons
//    public static final String OUT_MF07  = "out_mf07";
//    public static final String OUT_MF13  = "out_mf13";
//    public static final String OUT_MF14  = "out_mf14";
//    public static final String OUT_MF16  = "out_mf16";



    //        Main.getPinHandler().setScheme("mf07", "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf13", "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf14", "5:on,1000;off,1000");
//        Main.getPinHandler().setScheme("mf16", "5:on,1000;off,1000");


    public static final String SIREN_TO_ANNOUNCE_THE_COLOR_CHANGE = "siren_to_announce_the_color_change";

    // nur für die RGB Flagge als Key für den PinHandler. Habs hier hin gepackt, damit das einheitlich ist.
    public static final String OUT_RGB_FLAG  = "rgbflag";

    public Configs() throws IOException {
        configs = new SortedProperties(); // Einstellungen, die verändert werden
        applicationContext = new Properties(); // inhalte der application.properties (von Maven)

        // defaults
        configs.put(MATCHID, "1");
        configs.put(SLEEP_PER_CYCLE, "500");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "OCF Flagge #" + new java.util.Random().nextInt());
        configs.put(GAMETIME, "0");
        configs.put(BUTTON_REACTION_TIME, "0");

        configs.put(NUMBER_OF_TEAMS, "2");
        configs.put(MAX_NUMBER_OF_TEAMS, "4");


        // Hardware Defaults
        // Buttons benutzen immer den Raspi GPIO Provider
        configs.put(BUTTON_STANDBY_ACTIVE, "GPIO 3");
        configs.put(BUTTON_PRESET_NUM_TEAMS, "GPIO 12");
        configs.put(BUTTON_PRESET_GAMETIME, "GPIO 13");
        configs.put(BUTTON_RESET, "GPIO 14");
        configs.put(BUTTON_RED, "GPIO 21");
        configs.put(BUTTON_BLUE, "GPIO 22");
        configs.put(BUTTON_GREEN, "GPIO 23");
        configs.put(BUTTON_YELLOW, "GPIO 24");
        configs.put(BUTTON_SHUTDOWN, "GPIO 25"); // Bei RASPI2 muss es der GPIO25 sein, beim RASPI3 der GPIO28. Sehr seltsam.

        // Alle anderen den MCP23017

        configs.put(OUT_LED_RED_BTN, "mf01");
        configs.put(OUT_LED_BLUE_BTN, "mf02");
        configs.put(OUT_LED_GREEN_BTN, "mf04");
        configs.put(OUT_LED_YELLOW_BTN, "mf05");
        configs.put(OUT_LED_GREEN, "mf03");
        configs.put(OUT_LED_WHITE, "mf06");


        configs.put(OUT_FLAG_WHITE, "mf08");
        configs.put(OUT_FLAG_RED, "mf13"); // actioncase mf09  // unused in ocfflag
        configs.put(OUT_FLAG_BLUE, "mf14"); // actioncase mf10   // unused in ocfflag
        configs.put(OUT_FLAG_GREEN, "mf16");
        configs.put(OUT_FLAG_YELLOW, "mf12");

        configs.put(OUT_SIREN_START_STOP, "mf09"); //mf09  // orig: rly01
        configs.put(OUT_SIREN_COLOR_CHANGE, "mf10"); // ocfflag2 mf10  // rly02
        configs.put(OUT_HOLDDOWN_BUZZER, "mf11");   // mf06
        configs.put(SIRENS_ENABLED, "true");

//        // RESERVE
//        configs.put(OUT_MF07, "mf07");
//        configs.put(OUT_MF13, "mf13");
//        configs.put(OUT_MF14, "mf14");
//        configs.put(OUT_MF16, "mf16");

        configs.put(REST_URL, "http://localhost:8090/rest/gamestate/create");
        configs.put(REST_AUTH, "Torsten:test1234");

        configs.put(TIME_ANNOUNCER, "true");
        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_GREEN, "10");
        configs.put(BRIGHTNESS_YELLOW, "10");
        configs.put(MIN_STAT_SEND_TIME, "0"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht
        configs.put(AIRSIREN_SIGNAL, "1:on,5000;off,1");
        configs.put(COLORCHANGE_SIREN_SIGNAL, "2:on,50;off,50");
//        configs.put(GAME_TIME_LIST, "600000,900000,1200000,1800000,3600000,5400000,7200000,9000000,10800000,12600000,14400000,16200000,17999000");
        configs.put(GAME_TIME_LIST, "1,10,15,20,30,45,60,75,90,105,120");

        configs.put(DISPLAY_RED_I2C, "0x72");
        configs.put(DISPLAY_BLUE_I2C, "0x71");
        configs.put(DISPLAY_WHITE_I2C, "0x73");
        configs.put(DISPLAY_YELLOW_I2C, "0x70");
        configs.put(DISPLAY_GREEN_I2C, "0x74");

        configs.put(FLAG_RGB_WHITE, "white");
        configs.put(FLAG_RGB_BLUE, "blue");
        configs.put(FLAG_RGB_GREEN, "green");
        configs.put(FLAG_RGB_RED, "red");
        configs.put(FLAG_RGB_YELLOW, "#ff8000");
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
        logger.setLevel(Level.toLevel(configs.getProperty(LOGLEVEL), Level.DEBUG));
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
            logger.fatal(ex);
            System.exit(1);
        }
    }

    public Long[] getGameTimes() {
        String[] listTimes = get(GAME_TIME_LIST).split("\\,");
        ArrayList<Long> list = new ArrayList<>();
        for (String time : listTimes) list.add(Long.parseLong(time));
        return list.toArray(new Long[list.size()]);
    }


}
