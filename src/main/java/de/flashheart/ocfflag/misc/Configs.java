package de.flashheart.ocfflag.misc;

import de.flashheart.ocfflag.Main;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
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
    // -1 bedeutet, FTP abschalten. 0 heisst immer weiter versuchen. Ansonsten die konkrete Anzahl
    public static final String FTPMAXERRORCOUNT = "ftp_maxerror_count";
    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String GAMETIME = "gametime";
    public static final String SLEEP_PER_CYCLE = "sleep_per_cycle";
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";
    public static final String BRIGHTNESS_YELLOW = "brightness_yellow";
    public static final String BRIGHTNESS_GREEN = "brightness_green";
    public static final String NUMBER_OF_TEAMS = "num_teams";
    public static final String AIRSIREN_SIGNAL = "airsiren_signal";
    public static final String COLORCHANGE_SIREN_SIGNAL = "colorchange_siren_signal";
    public static final String GAME_TIME_LIST = "game_time_list";

    public static final String TIME_ANNOUNCER = "time_announcer"; // schaltet das Blinken gemäß der Restspielzeit ein oder aus.

    public static final String APPCONTEXT_NOGPIO = "appctx_nogpio";

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
        configs.put(SLEEP_PER_CYCLE, "500");
        configs.put(NUMBER_OF_TEAMS, "2");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "OCF Flagge #" + new java.util.Random().nextInt());
        configs.put(GAMETIME, "0");
        configs.put(FTPS, "false");
        configs.put(FTPPORT, "21");
        configs.put(FTPMAXERRORCOUNT, "30");
        configs.put(TIME_ANNOUNCER, "true");
        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_GREEN, "10");
        configs.put(BRIGHTNESS_YELLOW, "10");
        configs.put(MIN_STAT_SEND_TIME, "0"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht
        configs.put(AIRSIREN_SIGNAL, "1:on,5000;off,1");
        configs.put(COLORCHANGE_SIREN_SIGNAL, "2:on,50;off,50");
        configs.put(GAME_TIME_LIST, "600000,900000,1200000,1800000,3600000,5400000,7200000,9000000,10800000,12600000,14400000,16200000,17999000");

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
        return getInt(FTPMAXERRORCOUNT) >= 0 && configs.containsKey(FTPUSER) && configs.containsKey(FTPHOST) && configs.containsKey(FTPPORT) && configs.containsKey(FTPPWD) && configs.containsKey(FTPS) && configs.containsKey(FTPREMOTEPATH);

    }


    public String getApplicationInfo(Object key) {
        return applicationContext.containsKey(key) ? applicationContext.get(key).toString() : "null";
    }

    public boolean is(Object key){
        return Boolean.parseBoolean(configs.containsKey(key) ? configs.get(key).toString() : "false");
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

    public Long[] getGameTimes() {
        String[] listTimes = get(GAME_TIME_LIST).split("\\,");
        ArrayList<Long> list = new ArrayList<>();
        for (String time : listTimes) list.add(Long.parseLong(time));
        return list.toArray(new Long[list.size()]);
    }


}
