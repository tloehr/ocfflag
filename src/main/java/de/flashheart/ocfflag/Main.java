package de.flashheart.ocfflag;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import de.flashheart.ocfflag.hardware.Display7Segments4Digits;
import de.flashheart.ocfflag.misc.SortedProperties;
import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.swing.FrameDebug;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Main {

    private static GpioController GPIO;
    private static FrameDebug frameDebug;
    private static SortedProperties config;

    private static Logger logger;
    private static Level logLevel = Level.DEBUG;


    public static void main(String[] args) throws Exception {

        initBaseSystem();
        initCommon();
        initDebugFrame();
        initRaspi();

        Display7Segments4Digits display7Segments4Digits = new Display7Segments4Digits(0x70, getFrameDebug().getLblBlue());
        display7Segments4Digits.setText("1627");

    }


    private static void initBaseSystem() {
        System.setProperty("logs", getWorkingPath());
//        System.setProperty("logs", Tools.getWorkingPath());
        logger = Logger.getLogger("Main");
        logger.setLevel(logLevel);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {

            }
        }));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            logger.fatal(e);
            logger.fatal(sw);
        });


    }


    /**
     * Diese Method enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen oder einem anderen Computer.
     *
     * @throws Exception
     */
    private static void initCommon() throws Exception {


        logger = Logger.getLogger("Main");
        logger.setLevel(logLevel);

        config = new SortedProperties();
        // todo: configreader needed
        config.put("vibeSensor1", "GPIO 4");
        config.put("HEALTH_CHANGE_PER_HIT", "-1");
        config.put("GAME_LENGTH_IN_SECONDS", "60");
        config.put("DELAY_BEFORE_GAME_STARTS_IN_SECONDS", "5");
        config.put("MAX_HEALTH", "1000");
        config.put("DEBOUNCE", "15");

        config.put("pwmRed", "GPIO 0");
        config.put("pwmGreen", "GPIO 3");
        config.put("pwmBlue", "GPIO 5");


    }

    /**
     * Dieses Init wird nur ausgeführt, wenn das Programm NICHT auf einem Raspi läuft.
     *
     * @throws Exception
     */
    private static void initDebugFrame() throws Exception {
//        if (isArm()) return;

        frameDebug = new FrameDebug();
        frameDebug.pack();
        frameDebug.setVisible(true);
        frameDebug.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private static void initRaspi() throws Exception {
        if (!isArm()) return;
        GPIO = GpioFactory.getInstance();

        Pin pinRed = RaspiPin.getPinByName(config.getProperty("pwmRed"));
        Pin pinGreen = RaspiPin.getPinByName(config.getProperty("pwmGreen"));
        Pin pinBlue = RaspiPin.getPinByName(config.getProperty("pwmBlue"));
    }

    public static Level getLogLevel() {
        return logLevel;
    }

    public static GpioController getGPIO() {
        return GPIO;
    }

    public static FrameDebug getFrameDebug() {
        return frameDebug;
    }

    private static String getWorkingPath() {
        return (isArm() ? "/home/pi" : System.getProperty("user.home")) + File.separator + "ocfflag";
    }

    // http://www.mkyong.com/java/how-to-detect-os-in-java-systemgetpropertyosname/
    public static boolean isArm() {

        String os = System.getProperty("os.arch").toLowerCase();
        return (os.indexOf("arm") >= 0);

    }
}
