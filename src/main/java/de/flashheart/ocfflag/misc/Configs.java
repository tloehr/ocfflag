package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

public class Configs {
    private final SortedProperties configs;
    private final Properties applicationContext;
    private final Logger logger = Logger.getLogger(getClass());


    public static final String MATCHID = "matchid";
    public static final String MYUUID = "uuid";
    public static final String LOGLEVEL = "loglevel";
    public static final String FTPHOST = "ftphost";
    public static final String FTPPORT = "ftpport";
    public static final String FTPUSER = "ftpuser";
    public static final String FTPPWD = "ftppwd";
    public static final String FTPS = "ftps";
    public static final String FTPREMOTEPATH = "ftpremotepath";
    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String GAMETIME = "gametime";
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";
    public static final String BRIGHTNESS_YELLOW = "brightness_yellow";
    public static final String BRIGHTNESS_GREEN = "brightness_green";
    public static final String NUMBER_OF_TEAMS = "num_teams";
    public static final String AIRSIREN_SIGNAL = "airsiren_signal";
    public static final String COLORCHANGE_SIREN_SIGNAL = "colorchange_siren_signal";

    public static final String FLAG_COLOR_WHITE = "flag_color_white";
    public static final String FLAG_COLOR_BLUE = "flag_color_blue";
    public static final String FLAG_COLOR_RED = "flag_color_red";
    public static final String FLAG_COLOR_GREEN = "flag_color_green";
    public static final String FLAG_COLOR_YELLOW = "flag_color_yellow";
    
    public Configs() throws IOException {
        configs = new SortedProperties(); // Einstellungen, die verändert werden
        applicationContext = new Properties(); // inhalte der application.properties (von Maven)

        // defaults
        configs.put(MATCHID, "1");
        configs.put(NUMBER_OF_TEAMS, "2");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "OCF Flagge #" + new java.util.Random().nextInt());
        configs.put(GAMETIME, "0");
        configs.put(FTPS, "false");
        configs.put(FTPPORT, "21");
        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_GREEN, "10");
        configs.put(BRIGHTNESS_YELLOW, "10");
        configs.put(MIN_STAT_SEND_TIME, "0"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht
        configs.put(AIRSIREN_SIGNAL, "1:on,5000;off,1");
        configs.put(COLORCHANGE_SIREN_SIGNAL, "2:on,50;off,50");

        configs.put(FLAG_COLOR_WHITE, "white");
        configs.put(FLAG_COLOR_BLUE, "blue");
        configs.put(FLAG_COLOR_GREEN, "green");
        configs.put(FLAG_COLOR_RED, "red");
        configs.put(FLAG_COLOR_YELLOW, "#ff8000");

        // configdatei einlesen
        loadConfigs();
        loadApplicationContext();

        // und der Rest
        logger.setLevel(Level.toLevel(configs.getProperty(LOGLEVEL), Level.DEBUG));
        if (!configs.containsKey(MYUUID)) {
            configs.put(MYUUID, UUID.randomUUID().toString());
        }
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

    public boolean isFTPComplete() {
        return configs.containsKey(FTPUSER) && configs.containsKey(FTPHOST) && configs.containsKey(FTPPORT) && configs.containsKey(FTPPWD) && configs.containsKey(FTPS) && configs.containsKey(FTPREMOTEPATH);
    }


    public String getApplicationInfo(Object key) {
        return applicationContext.containsKey(key) ? applicationContext.get(key).toString() : "null";
    }

    public int getInt(Object key) {
            return Integer.parseInt(configs.containsKey(key) ? configs.get(key).toString() : "-1");
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


}
