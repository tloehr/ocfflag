package de.flashheart.ocfflag;

import de.flashheart.ocfflag.gamemodes.Game;
import de.flashheart.ocfflag.gamemodes.GameSelector;
import de.flashheart.ocfflag.gui.FrameDebug;
import de.flashheart.ocfflag.gui.LCDTextDisplay;
import de.flashheart.ocfflag.hardware.MySystem;
import de.flashheart.ocfflag.misc.Configs;
import de.flashheart.ocfflag.misc.Tools;
import org.apache.commons.cli.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

public class Main {
    private static Game currentGame;
    private static Logger logger;
    private static Level logLevel = Level.DEBUG;
    private static final HashMap<String, Object> applicationContext = new HashMap<>();
    private static Configs configs;

    public static void main(String[] args) throws Exception {

        initBaseSystem(args);

        UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());


        applicationContext.put(Configs.MY_SYSTEM, new MySystem());
        ((FrameDebug) Main.getFromContext(Configs.FRAME_DEBUG)).setVisible(true);

        setGame(new GameSelector());
    }

    public static Object getFromContext(String key) {
        return applicationContext.get(Tools.catchNull(key));
    }

    public static void addToContext(String key, Object value) {
        applicationContext.put(key, value);
    }

    public static String getFromConfigs(String key) {
        return configs.get(key);
    }


    /**
     * Diese Methode enthält alles was initialisiert werden muss, gleich ob wir das Programm auf einem Raspi ausführen
     * oder einem anderen Computer.
     *
     * @param args
     */
    private static void initBaseSystem(String[] args) throws IOException {
        System.setProperty("logs", Tools.getWorkingPath());
        configs = new Configs();
        applicationContext.put(Configs.THE_CONFIGS, configs);

        Logger.getRootLogger().setLevel(logLevel);
        logger = Logger.getLogger("Main");
//
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            ((MySystem) Main.getFromContext(Configs.MY_SYSTEM)).shutdown();
//        }));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            sw.toString(); // stack trace as a string
            logger.fatal(e);
            logger.fatal(sw);
        });

        String title = "RLG-System " + configs.getApplicationInfo("my.version") + " [" + configs.getApplicationInfo("buildNumber") + "]";

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
            f.printHelp("rlgs-1.1.jar [OPTION]", "RLGS, Version " + configs.getApplicationInfo("my.version"), opts, footer);
            System.exit(0);
        }

        if (cl.hasOption("h")) {
            HelpFormatter f = new HelpFormatter();
            f.printHelp("rlgs-1.1.jar [OPTION]", "RLGS, Version " + configs.getApplicationInfo("my.version"), opts, footer);
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

        addToContext(Configs.IGNORE_GPIO_IN_ARM_MODE, cl.hasOption("n"));
        addToContext(Configs.DEV_MODE, cl.hasOption("n"));

    }

    public static Game getCurrentGame() {
        return currentGame;
    }

    public static void setGame(Game game) {
        int lcdpage_for_config_buttons = Integer.parseInt(applicationContext.getOrDefault(Configs.LCDPAGE_FOR_CONFIG_BUTTONS, "-1").toString());
        if (lcdpage_for_config_buttons == -1) {
            // es gibt noch keine Seite für die Config-Tasten.
            // wird jetzt einmal erstellt, und dann für alle verwendet.
            lcdpage_for_config_buttons = ((LCDTextDisplay) getFromContext(Configs.LCD_TEXT_DISPLAY)).add_page();
            applicationContext.put(Configs.LCDPAGE_FOR_CONFIG_BUTTONS, lcdpage_for_config_buttons);
        }
        if (currentGame != null) currentGame.stop_gamemode();
        currentGame = game;
        currentGame.start_gamemode();
    }
}
