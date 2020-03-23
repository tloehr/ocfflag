package de.flashheart.ocfflag;

import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.mechanics.GameSelector;
import de.flashheart.ocfflag.mechanics.Games;
import de.flashheart.ocfflag.mechanics.OCF;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import de.flashheart.ocfflag.statistics.MessageProcessor;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class Main {
    private static Games currentGame;
    private static Logger logger;
    private static Level logLevel = Level.DEBUG;
    private static final HashMap<String, Object> applicationContext = new HashMap<>();
    private static Configs configs;

//    private static boolean ignore_gpio; // ignore gpios, even when running on raspi
//    private static boolean dev_mode; // show develop mode functions
//
////    private static MessageProcessor messageProcessor;
//    public static MessageProcessor getMessageProcessor() {
//        return null;
//    }

    public static void main(String[] args) throws Exception {

        initBaseSystem(args);

        UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());

        FrameDebug frameDebug = new FrameDebug();
        applicationContext.put(Configs.FRAME_DEBUG, frameDebug);
        // Das Fenster brauchen wir nur, wenn wir einen Raspi benutzen.
        frameDebug.getBtnTestDialog().setVisible(Tools.isArm());
        frameDebug.setVisible(true);

        applicationContext.put(Configs.MY_SYSTEM, new MySystem());

        setGame(new OCF(2));
    }

    public static Object getFromContext(String key) {
        return applicationContext.get(key);
    }

    public static void addToContext(String key, Object value) {
        applicationContext.put(key, value);
    }

    public static String getFromConfigs(String key) {
        return configs.get(key);
    }

    public static void addToConfigs(String key, String value) {
        configs.put(key, value);
    }

    /**
     * Diese Methode enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen
     * oder einem anderen Computer.
     *
     * @param args
     */

    private static void initBaseSystem(String[] args) throws IOException {
        System.setProperty("logs", Tools.getWorkingPath());
        configs  = new Configs();
        applicationContext.put("configs", configs);

        Logger.getRootLogger().setLevel(logLevel);
        logger = Logger.getLogger("Main");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            prepareShutdown();
        }));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            logger.fatal(e);
            logger.fatal(sw);
        });

        String title = "ocfflag " + configs.getApplicationInfo("my.version") + " [" + configs.getApplicationInfo("buildNumber") + "]";

        logger.info(title);

        /***
         *       ____                                          _   _     _               ___        _   _
         *      / ___|___  _ __ ___  _ __ ___   __ _ _ __   __| | | |   (_)_ __   ___   / _ \ _ __ | |_(_) ___  _ __  ___
         *     | |   / _ \| '_ ` _ \| '_ ` _ \ / _` | '_ \ / _` | | |   | | '_ \ / _ \ | | | | '_ \| __| |/ _ \| '_ \/ __|
         *     | |__| (_) | | | | | | | | | | | (_| | | | | (_| | | |___| | | | |  __/ | |_| | |_) | |_| | (_) | | | \__ \
         *      \____\___/|_| |_| |_|_| |_| |_|\__,_|_| |_|\__,_| |_____|_|_| |_|\___|  \___/| .__/ \__|_|\___/|_| |_|___/
         *                                                                                   |_|
         */
        Options opts = new Options();
        opts.addOption("h", "help", false, Tools.xx("cmdline.help.description"));
        opts.addOption("v", "version", false, Tools.xx("cmdline.version.description"));
        opts.addOption("n", "ignoregpio", false, Tools.xx("cmdline.ignoregpio.description"));
        opts.addOption("d", "devmode", false, Tools.xx("cmdline.devmode.description"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cl = null;

        String footer = "https://www.flashheart.de" + " " + configs.getApplicationInfo("buildNumber");

        /***
         *      _          _
         *     | |__   ___| |_ __    ___  ___ _ __ ___  ___ _ __
         *     | '_ \ / _ \ | '_ \  / __|/ __| '__/ _ \/ _ \ '_ \
         *     | | | |  __/ | |_) | \__ \ (__| | |  __/  __/ | | |
         *     |_| |_|\___|_| .__/  |___/\___|_|  \___|\___|_| |_|
         *                  |_|
         */
        try {
            cl = parser.parse(opts, args);
        } catch (ParseException ex) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("ocfflag-1.1.jar [OPTION]", "ocfflag, Version " + configs.getApplicationInfo("my.version"), opts, footer);
            System.exit(0);
        }

        if (cl.hasOption("h")) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("ocfflag-1.1.jar [OPTION]", "ocfflag, Version " + configs.getApplicationInfo("my.version"), opts, footer);
            System.exit(0);
        }

        /***
         *     __     __            _
         *     \ \   / /__ _ __ ___(_) ___  _ __
         *      \ \ / / _ \ '__/ __| |/ _ \| '_ \
         *       \ V /  __/ |  \__ \ | (_) | | | |
         *        \_/ \___|_|  |___/_|\___/|_| |_|
         *
         */

        if (cl.hasOption("v")) {
            System.out.println(title);
            System.out.println(footer);
            System.exit(0);
        }

        addToContext(Configs.IGNORE_GPIO_IN_ARM_MODE, new Boolean(cl.hasOption("n")));
        addToContext(Configs.DEV_MODE, new Boolean(cl.hasOption("n")));

//        if (cl.hasOption("n")) {
//            applicationContext.put(Configs.APPCONTEXT_NOGPIO, Boolean.TRUE);
//        } else {
//            applicationContext.put(Configs.APPCONTEXT_NOGPIO, System.getProperty("os.arch").toLowerCase().indexOf("arm") >= 0 ? Boolean.FALSE : Boolean.TRUE);
//        }
//


//        REACTION_TIME = configs.getLong(Configs.BUTTON_REACTION_TIME);

//        messageProcessor = new MessageProcessor();
//        messageProcessor.start();

    }

    public static void prepareShutdown() {

//        messageProcessor.interrupt();
        logger.info("\n  _____ _   _ ____     ___  _____    ___   ____ _____ _____ _             \n" +
                " | ____| \\ | |  _ \\   / _ \\|  ___|  / _ \\ / ___|  ___|  ___| | __ _  __ _ \n" +
                " |  _| |  \\| | | | | | | | | |_    | | | | |   | |_  | |_  | |/ _` |/ _` |\n" +
                " | |___| |\\  | |_| | | |_| |  _|   | |_| | |___|  _| |  _| | | (_| | (_| |\n" +
                " |_____|_| \\_|____/   \\___/|_|      \\___/ \\____|_|   |_|   |_|\\__,_|\\__, |\n" +
                "                                                                    |___/ ");
    }


//    public static boolean isIgnore_gpio() {
//        return ignore_gpio;
//    }
//
//    public static boolean isDev_mode() {
//        return dev_mode;
//    }

    public static Games getCurrentGame() {
        return currentGame;
    }

    public static void setGame(Games game) {
        currentGame = game;
    }
}
