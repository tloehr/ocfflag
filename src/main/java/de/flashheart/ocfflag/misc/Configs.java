package de.flashheart.ocfflag.misc;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class Configs {
    private final SortedProperties configs;
    private final Logger logger = Logger.getLogger(getClass());

    public static final String MATCHID = "matchid";
    public static final String MYUUID = "uuid";
    public static final String LOGLEVEL = "loglevel";
    public static final String FTPHOST = "ftphost";
    public static final String FTPPORT = "ftpport";
    public static final String FTPUSER = "ftpuser";
    public static final String FTPPWD = "ftppwd";
    public static final String MIN_STAT_SEND_TIME = "sendstats";
    public static final String FLAGNAME = "flagname";
    public static final String GAMETIME = "gametime";
    public static final String BRIGHTNESS_WHITE = "brightness_white";
    public static final String BRIGHTNESS_BLUE = "brightness_blue";
    public static final String BRIGHTNESS_RED = "brightness_red";

    public Configs() throws IOException {
        configs = new SortedProperties();
        // defaults
        configs.put(MATCHID, "1");
        configs.put(LOGLEVEL, "debug");
        configs.put(FLAGNAME, "Eine brandneue OCF Flagge");
        configs.put(GAMETIME, "0");
        configs.put(BRIGHTNESS_WHITE, "10");
        configs.put(BRIGHTNESS_BLUE, "10");
        configs.put(BRIGHTNESS_RED, "10");
        configs.put(MIN_STAT_SEND_TIME, "60000"); // in Millis, wie oft sollen die Stastiken spätestens gesendet werden. 0 = gar nicht

        // configdatei einlesen
        loadConfigs();

        // und der Rest
        logger.setLevel(Level.toLevel(configs.getProperty(LOGLEVEL), Level.DEBUG));
        if (!configs.containsKey(MYUUID)) {
            configs.put(MYUUID, UUID.randomUUID().toString());
        }
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

    public String get(Object key) {
        return configs.get(key).toString();
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